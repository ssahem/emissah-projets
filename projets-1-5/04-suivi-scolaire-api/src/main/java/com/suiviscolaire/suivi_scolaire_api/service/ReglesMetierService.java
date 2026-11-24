package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.response.ClotureResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PoidsResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ReouvertureResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.HistoriqueReouverture;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.entity.TypeEvaluation;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.HistoriqueReouvertureRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TypeEvaluationRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Règles métier sur les poids et les trimestres (F13, F14, F15).
 *
 * COUCHE       : Service
 * RÔLE         : modifier le poids des évaluations, clôturer un trimestre complet et rouvrir
 *                le dernier trimestre clôturé en journalisant la correction.
 * TRANSACTION  : lecture seule par défaut ; les trois cas d'usage écrivants portent
 *                @Transactional.
 * DÉPEND DE    : repositories de référence, TrimestreLookup, HistoriqueReouvertureRepository,
 *                Clock.
 * UTILISÉ PAR  : TypeEvaluationController, TrimestreController.
 * NE FAIT PAS  : de HTTP ni de FK de note vers trimestre (A-03) ; l'auteur est un texte libre
 *                (A-04).
 */
@Service
@Transactional(readOnly = true)
public class ReglesMetierService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final int LONGUEUR_MIN_MOTIF = 20;

    /** Alias acceptés pour l'identifiant {code} des types d'évaluation (F13). */
    private static final Map<String, String> ALIAS_TYPES = Map.of(
            "DEV", "Devoir", "DEVOIR", "Devoir",
            "INTERRO", "Interrogation", "INTERROGATION", "Interrogation",
            "ORAL", "Oral");

    private final TypeEvaluationRepository typeEvaluationRepository;
    private final TrimestreRepository trimestreRepository;
    private final EnseignementRepository enseignementRepository;
    private final NoteRepository noteRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final HistoriqueReouvertureRepository historiqueRepository;
    private final TrimestreLookup trimestreLookup;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances par constructeur. */
    public ReglesMetierService(TypeEvaluationRepository typeEvaluationRepository,
                               TrimestreRepository trimestreRepository,
                               EnseignementRepository enseignementRepository,
                               NoteRepository noteRepository, ClasseRepository classeRepository,
                               MatiereRepository matiereRepository,
                               HistoriqueReouvertureRepository historiqueRepository,
                               TrimestreLookup trimestreLookup, Clock clock) {
        this.typeEvaluationRepository = typeEvaluationRepository;
        this.trimestreRepository = trimestreRepository;
        this.enseignementRepository = enseignementRepository;
        this.noteRepository = noteRepository;
        this.classeRepository = classeRepository;
        this.matiereRepository = matiereRepository;
        this.historiqueRepository = historiqueRepository;
        this.trimestreLookup = trimestreLookup;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * F13 — Modifie le poids d'un type d'évaluation. Le code est un alias lisible (DEV, INTERRO,
 * ORAL), le libellé exact ou l'identifiant numérique ; la clôture d'un trimestre fige les
 *   poids.
     */
    @Transactional
    public PoidsResponse modifierPoids(String code, BigDecimal poids) {
        // ── Étape 1/3 : refuser un poids nul ou négatif avant toute écriture ──
        if (poids == null || poids.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DonneeInvalideException("poids-invalide",
                    "Le poids doit être strictement positif", Map.of("poids", String.valueOf(poids)));
        }
        // ── Étape 2/3 : aucuns poids ne bouge après la première clôture ──
        if (trimestreRepository.existsByEstClotureTrue()) {
            throw new ConflitMetierException("poids-fige",
                    "Le poids n'est plus modifiable : un trimestre est déjà clôturé");
        }
        // ── Étape 3/3 : résoudre le type puis persister le nouveau poids ──
        TypeEvaluation type = resoudreType(code);
        type.modifierPoids(poids);
        return new PoidsResponse(code, type.getLibelle(), type.getPoids());
    }

    /**
 * F14 — Clôture un trimestre seulement si chaque enseignement de chaque classe possède au
 *   moins
 * une note pour ce trimestre ; la réponse 409 porte la liste des classes et matières
 *   manquantes.
     */
    @Transactional
    public ClotureResponse cloturer(Integer numero) {
        Trimestre trimestre = trimestreLookup.required(numero);
        if (trimestre.isEstCloture()) {
            throw new ConflitMetierException("trimestre-deja-cloture",
                    "Le trimestre " + numero + " est déjà clôturé", Map.of("trimestre", numero));
        }
        List<Enseignement> enseignements = enseignementRepository.findAll();
        List<Map<String, Object>> manquants = new ArrayList<>();
        for (Enseignement enseignement : enseignements) {
            if (noteRepository.existsByIdEnseignementAndTrimestre(
                    enseignement.getIdEnseignement(), numero)) {
                continue;
            }
            Map<String, Object> manquant = new LinkedHashMap<>();
            manquant.put("idClasse", enseignement.getIdClasse());
            manquant.put("nomClasse", classeRepository.findById(enseignement.getIdClasse())
                    .map(c -> c.getNom()).orElse(null));
            manquant.put("idMatiere", enseignement.getIdMatiere());
            manquant.put("libelleMatiere", matiereRepository.findById(enseignement.getIdMatiere())
                    .map(m -> m.getLibelle()).orElse(null));
            manquants.add(manquant);
        }
        if (!manquants.isEmpty()) {
            throw new ConflitMetierException("trimestre-cloture-incomplete",
                    "Clôture refusée : " + manquants.size()
                            + " enseignement(s) n'ont aucune note pour ce trimestre",
                    Map.of("trimestre", numero, "manquants", manquants));
        }
        trimestre.cloturer(LocalDate.now(clock));
        return new ClotureResponse(numero, trimestre.getDateCloture(), enseignements.size());
    }

    /**
     * F15 — Rouvre le dernier trimestre clôturé : le motif fait au moins 20 caractères, le
     * trimestre visé doit être clôturé, être le plus récent et ne pas avoir déjà été rouvert.
     */
    @Transactional
    public ReouvertureResponse reouvrir(Integer numero, String motif, String auteur) {
        // ── Étape 1/4 : le motif est obligatoire et suffisamment explicite ──
        if (motif == null || motif.trim().length() < LONGUEUR_MIN_MOTIF) {
            throw new DonneeInvalideException("motif-trop-court",
                    "Le motif doit contenir au moins " + LONGUEUR_MIN_MOTIF + " caractères",
                    Map.of("longueurMinimale", LONGUEUR_MIN_MOTIF));
        }
        Trimestre trimestre = trimestreLookup.required(numero);
        // ── Étape 2/4 : un trimestre ouvert ne peut pas être « rouvert » ──
        if (!trimestre.isEstCloture()) {
            throw new ConflitMetierException("trimestre-non-cloture",
                    "Le trimestre " + numero + " n'est pas clôturé",
                    Map.of("trimestre", numero));
        }
        // ── Étape 3/4 : seul le dernier trimestre clôturé est corrigeable ──
        List<Trimestre> clotures = trimestreRepository.findCloturesDuPlusRecentAuPlusAncien();
        if (clotures.isEmpty() || !clotures.get(0).getIdTrimestre().equals(trimestre.getIdTrimestre())) {
            Integer dernier = clotures.isEmpty() ? null : clotures.get(0).getNumero();
            throw new ConflitMetierException("trimestre-non-derniere-cloture",
                    "Seul le dernier trimestre clôturé peut être rouvert (trimestre " + dernier + ")",
                    Map.of("dernierTrimestreCloture", dernier));
        }
        if (historiqueRepository.findByIdTrimestre(trimestre.getIdTrimestre()).isPresent()) {
            throw new ConflitMetierException("trimestre-deja-reouvert",
                    "Ce trimestre a déjà été rouvert", Map.of("trimestre", numero));
        }
        // ── Étape 4/4 : journaliser puis rouvrir ──
        HistoriqueReouverture trace = historiqueRepository.save(new HistoriqueReouverture(
                trimestre.getIdTrimestre(), LocalDate.now(clock), motif.trim(), auteur.trim()));
        trimestre.reouvrir();
        return new ReouvertureResponse(trace.getIdHistorique(), numero,
                trace.getDateReouverture(), trace.getMotif(), trace.getAuteur());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    /** Résout un code F13 par alias, par libellé (cas indifférent) ou par identifiant numérique. */
    private TypeEvaluation resoudreType(String code) {
        if (code == null || code.isBlank()) {
            throw new DonneeInvalideException("type-evaluation-code-manquant",
                    "Le code du type d'évaluation est obligatoire");
        }
        String cle = code.trim();
        String libelleAlias = ALIAS_TYPES.get(cle.toUpperCase());
        TypeEvaluation type = null;
        if (libelleAlias != null) {
            type = typeEvaluationRepository.findByLibelle(libelleAlias).orElse(null);
        }
        if (type == null) {
            type = typeEvaluationRepository.findByLibelleIgnoreCase(cle).orElse(null);
        }
        if (type == null && cle.chars().allMatch(Character::isDigit)) {
            type = typeEvaluationRepository.findById(Integer.valueOf(cle)).orElse(null);
        }
        if (type == null) {
            throw new DonneeInvalideException("type-evaluation-inconnu",
                    "Le type d'évaluation « " + code + " » est inconnu", Map.of("code", code));
        }
        return type;
    }
}

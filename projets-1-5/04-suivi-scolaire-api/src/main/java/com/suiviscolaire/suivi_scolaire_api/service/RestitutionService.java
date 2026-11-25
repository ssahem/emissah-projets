package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.response.ClassementResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.DetailCoefficientResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.LigneClassementResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyenneGeneraleResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyenneMatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyennesResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.VueRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Restitution des moyennes et du classement (F17, F18, F19).
 *
 * COUCHE       : Service
 * RÔLE         : exploiter les vues SQL en lecture seule pour produire moyenne par matière,
 *                moyenne générale détaillée et classement d'une classe (ex æquo, élèves sans
 *                note).
 * TRANSACTION  : lecture seule : aucune écriture n'est faite dans les vues.
 * DÉPEND DE    : VueRepository (JdbcTemplate), repositories de référence, TrimestreLookup.
 * UTILISÉ PAR  : EleveController (F17, F18) et ClasseController (F19).
 * NE FAIT PAS  : de SQL dans le service hors repository dédié, ni de fausse moyenne à zéro.
 */
@Service
@Transactional(readOnly = true)
public class RestitutionService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final int DECIMALES = 2;

    private final VueRepository vueRepository;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClasseRepository classeRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;
    private final TrimestreLookup trimestreLookup;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances par constructeur. */
    public RestitutionService(VueRepository vueRepository, EleveRepository eleveRepository,
                              InscriptionRepository inscriptionRepository,
                              ClasseRepository classeRepository,
                              EnseignementRepository enseignementRepository,
                              MatiereRepository matiereRepository, NoteRepository noteRepository,
                              TrimestreLookup trimestreLookup) {
        this.vueRepository = vueRepository;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.classeRepository = classeRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.noteRepository = noteRepository;
        this.trimestreLookup = trimestreLookup;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    // ── moyennesParMatiere() ─────────────────────────────────────────────────

    /**
     * F17 — Moyenne de l'élève dans chaque matière enseignée dans sa classe.
     * Une matière sans note est renvoyée avec moyenne null et evaluee=false ;
     * les moyennes viennent de v_moyenne_matiere.
     */

    public MoyennesResponse moyennesParMatiere(Integer idEleve, Integer trimestre) {
        eleveRepository.findById(idEleve)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", idEleve));
        trimestreLookup.required(trimestre);
        Inscription inscription = inscriptionCourante(idEleve);
        Map<Integer, BigDecimal> moyennesVues = new HashMap<>();
        vueRepository.moyennesMatiere(inscription.getIdInscription(), trimestre).forEach(ligne -> {
            BigDecimal moyenne = (BigDecimal) ligne.get("moyenne_matiere");
            if (moyenne != null && ligne.get("id_enseignement") != null) {
                moyennesVues.put(((Number) ligne.get("id_enseignement")).intValue(), moyenne);
            }
        });
        List<MoyenneMatiereResponse> matieres = new ArrayList<>();
        for (Enseignement enseignement : enseignementRepository
                .findAllByIdClasseOrderByIdEnseignementAsc(inscription.getIdClasse())) {
            Matiere matiere = matiereRepository.findById(enseignement.getIdMatiere())
                    .orElseThrow(() -> new RessourceIntrouvableException(
                            "Matiere", enseignement.getIdMatiere()));
            long nombreNotes = noteRepository
                    .countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(
                            enseignement.getIdEnseignement(), trimestre,
                            inscription.getNumeroInscription());
            boolean evaluee = nombreNotes > 0;
            matieres.add(new MoyenneMatiereResponse(matiere.getIdMatiere(), matiere.getLibelle(),
                    matiere.getCoefficient(), evaluee ? moyennesVues.get(enseignement.getIdEnseignement()) : null,
                    (int) nombreNotes, evaluee));
        }
        return new MoyennesResponse(idEleve, trimestre, matieres);
    }

    // ── moyenneGenerale() ────────────────────────────────────────────────────

    /**
     * F18 — Moyenne générale pondérée par les coefficients des matières (vue
     * v_moyenne_generale), avec le détail du calcul. Sans aucune note, la
     * moyenne est absente (null), jamais zéro.
     */

    public MoyenneGeneraleResponse moyenneGenerale(Integer idEleve, Integer trimestre) {
        eleveRepository.findById(idEleve)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", idEleve));
        trimestreLookup.required(trimestre);
        Inscription inscription = inscriptionCourante(idEleve);
        BigDecimal moyenne = vueRepository.moyenneGenerale(inscription.getIdInscription(), trimestre);
        List<DetailCoefficientResponse> detail = new ArrayList<>();
        for (MoyenneMatiereResponse matiere : moyennesParMatiere(idEleve, trimestre).matieres()) {
            if (!matiere.evaluee() || matiere.moyenne() == null || matiere.coefficient() == null) {
                continue;
            }
            BigDecimal contribution = matiere.moyenne().multiply(matiere.coefficient())
                    .setScale(DECIMALES, RoundingMode.HALF_UP);
            detail.add(new DetailCoefficientResponse(matiere.idMatiere(), matiere.libelleMatiere(),
                    matiere.moyenne(), matiere.coefficient(), contribution));
        }
        return new MoyenneGeneraleResponse(idEleve, trimestre, moyenne, detail);
    }

    // ── classement() ─────────────────────────────────────────────────────────

    /**
     * F19 — Classement d'une classe : rangs densifiés de la vue pour les élèves
     * évalués, élèves sans note en fin de liste sans rang, moyenne de classe,
     * meilleure et plus faible moyenne.
     */

    public ClassementResponse classement(Integer idClasse, Integer trimestre) {
        Classe classe = classeRepository.findById(idClasse)
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", idClasse));
        trimestreLookup.required(trimestre);
        Map<Integer, Map<String, Object>> rangsVus = new HashMap<>();
        for (Map<String, Object> ligne : vueRepository.rangsClasse(idClasse, trimestre)) {
            rangsVus.put(((Number) ligne.get("id_inscription")).intValue(), ligne);
        }
        List<LigneClassementResponse> lignes = new ArrayList<>();
        List<BigDecimal> moyennes = new ArrayList<>();
        for (Inscription inscription : inscriptionRepository
                .findAllByIdClasseAndDateFinIsNull(idClasse)) {
            Eleve eleve = eleveRepository.findById(inscription.getIdEleve()).orElse(null);
            if (eleve == null) {
                continue;
            }
            Map<String, Object> vue = rangsVus.get(inscription.getIdInscription());
            BigDecimal moyenne = vue == null ? null : (BigDecimal) vue.get("moyenne_generale");
            Integer rang = vue == null || vue.get("rang") == null
                    ? null : ((Number) vue.get("rang")).intValue();
            if (moyenne != null) {
                moyennes.add(moyenne);
            }
            lignes.add(new LigneClassementResponse(eleve.getIdEleve(), eleve.getNom(),
                    eleve.getPrenom(), inscription.getIdInscription(), moyenne, rang));
        }
        lignes.sort(Comparator
                .comparing(LigneClassementResponse::moyenneGenerale,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(LigneClassementResponse::nom)
                .thenComparing(LigneClassementResponse::prenom));
        BigDecimal moyenneClasse = moyennes.isEmpty() ? null
                : moyennes.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(moyennes.size()), DECIMALES, RoundingMode.HALF_UP);
        BigDecimal meilleure = moyennes.stream().max(BigDecimal::compareTo).orElse(null);
        BigDecimal plusFaible = moyennes.stream().min(BigDecimal::compareTo).orElse(null);
        return new ClassementResponse(classe.getIdClasse(), trimestre, moyenneClasse,
                meilleure, plusFaible, lignes);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════


    // ── inscriptionCourante() ────────────────────────────────────────────────

    /**
     * Retourne l'inscription courante d'un élève ou une erreur 400 explicite.
     */

    private Inscription inscriptionCourante(Integer idEleve) {
        return inscriptionRepository.findByIdEleveAndDateFinIsNull(idEleve)
                .orElseThrow(() -> new DonneeInvalideException("inscription-manquante",
                        "L'élève " + idEleve + " n'a aucune inscription courante"));
    }
}

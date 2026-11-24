package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.response.AlerteDifficulteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.RapportAlertesResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.AlerteDifficulte;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.repository.AlerteDifficulteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.VueRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Détection hebdomadaire des élèves en difficulté (F16, arbitrage A-05).
 *
 * COUCHE       : Service
 * RÔLE         : repérer les élèves dont la moyenne du trimestre en cours est inférieure à 8
 *                ou a chuté de plus de 4 points, et persister une alerte par inscription et
 *                trimestre.
 * TRANSACTION  : lecture seule pour l'historique ; la détection écrivante est
 *                transactionnelle.
 * DÉPEND DE    : VueRepository (vues en lecture seule), repositories d'inscription et
 *                d'alerte, Clock.
 * UTILISÉ PAR  : le planificateur Spring et AlerteController pour la consultation.
 * NE FAIT PAS  : de HTTP, ni d'écriture dans les vues, ni de duplication d'une alerte
 *                existante.
 */
@Service
@Transactional(readOnly = true)
public class AlertesDifficulteService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Logger log = LoggerFactory.getLogger(AlertesDifficulteService.class);

    private static final BigDecimal SEUIL_MOYENNE = new BigDecimal("8.00");
    private static final BigDecimal SEUIL_CHUTE = new BigDecimal("4.00");

    private final VueRepository vueRepository;
    private final AlerteDifficulteRepository alerteRepository;
    private final InscriptionRepository inscriptionRepository;
    private final TrimestreRepository trimestreRepository;
    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final Clock clock;
    private final String cron;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances et la fréquence configurée de la tâche. */
    public AlertesDifficulteService(VueRepository vueRepository,
                                    AlerteDifficulteRepository alerteRepository,
                                    InscriptionRepository inscriptionRepository,
                                    TrimestreRepository trimestreRepository,
                                    EleveRepository eleveRepository,
                                    ClasseRepository classeRepository, Clock clock,
                                    @Value("${suivi-scolaire.alertes-difficulte.cron}") String cron) {
        this.vueRepository = vueRepository;
        this.alerteRepository = alerteRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.trimestreRepository = trimestreRepository;
        this.eleveRepository = eleveRepository;
        this.classeRepository = classeRepository;
        this.clock = clock;
        this.cron = cron;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * F16 — Tâche planifiée hebdomadaire. Détermine le trimestre en cours à partir de l'horloge
 * applicative, puis crée une alerte pour chaque élève concerné qui n'a pas encore été
 *   signalé.
     */
    @Scheduled(cron = "${suivi-scolaire.alertes-difficulte.cron}")
    @Transactional
    public RapportAlertesResponse detecter() {
        LocalDate aujourdHui = LocalDate.now(clock);
        Trimestre enCours = trimestreRepository.findContenant(aujourdHui).stream()
                .findFirst().orElse(null);
        if (enCours == null) {
            log.info("Aucun trimestre en cours le {} : détection F16 ignorée", aujourdHui);
            return new RapportAlertesResponse(aujourdHui, null, 0, 0, List.of());
        }
        Integer numero = enCours.getNumero();
        List<AlerteDifficulteResponse> creees = new ArrayList<>();
        int dejaPresentes = 0;
        for (Inscription inscription : inscriptionRepository.findAll()) {
            if (inscription.getDateFin() != null) {
                continue;
            }
            BigDecimal moyenne = vueRepository.moyenneGenerale(inscription.getIdInscription(), numero);
            if (moyenne == null) {
                continue;
            }
            BigDecimal precedent = numero > 1
                    ? vueRepository.moyenneGenerale(inscription.getIdInscription(), numero - 1) : null;
            BigDecimal ecart = precedent == null ? BigDecimal.ZERO.setScale(2)
                    : precedent.subtract(moyenne);
            boolean moyenneFaible = moyenne.compareTo(SEUIL_MOYENNE) < 0;
            boolean chuteForte = precedent != null && ecart.compareTo(SEUIL_CHUTE) > 0;
            if (!moyenneFaible && !chuteForte) {
                continue;
            }
            if (alerteRepository.findByIdInscriptionAndNumeroTrimestre(
                    inscription.getIdInscription(), numero).isPresent()) {
                dejaPresentes++;
                continue;
            }
            AlerteDifficulte alerte = alerteRepository.save(new AlerteDifficulte(
                    inscription.getIdInscription(), numero, moyenne, ecart, aujourdHui));
            creees.add(toResponse(alerte, motif(moyenneFaible, chuteForte, moyenne, ecart)));
        }
        log.info("Détection F16 du {} : {} alerte(s) créée(s), {} déjà présente(s)",
                aujourdHui, creees.size(), dejaPresentes);
        return new RapportAlertesResponse(aujourdHui, numero, creees.size(), dejaPresentes, creees);
    }

    /** Consultation des alertes déjà produites pour un trimestre (endpoint facultatif F16). */
    public List<AlerteDifficulteResponse> consulter(Integer numero) {
        if (numero == null || numero < 1 || numero > 3) {
            throw new DonneeInvalideException("trimestre-numero-invalide",
                    "Le numéro de trimestre doit être compris entre 1 et 3");
        }
        return alerteRepository.findAllByNumeroTrimestreOrderByIdAlerteAsc(numero).stream()
                .map(this::toResponseSansMotif).toList();
    }

    /** Fréquence effective de la tâche (utile à la documentation et aux tests). */
    public String getCron() { return cron; }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit le motif lisible d'une alerte. */
    private String motif(boolean moyenneFaible, boolean chuteForte,
                         BigDecimal moyenne, BigDecimal ecart) {
        if (moyenneFaible && chuteForte) {
            return "Moyenne " + moyenne + " inférieure à 8 et chute de " + ecart + " points";
        }
        if (moyenneFaible) {
            return "Moyenne " + moyenne + " inférieure à 8";
        }
        return "Chute de " + ecart + " points depuis le trimestre précédent";
    }

    /** Enrichit une alerte avec son motif, son élève et sa classe. */
    private AlerteDifficulteResponse toResponse(AlerteDifficulte alerte, String motif) {
        AlerteDifficulteResponse base = toResponseSansMotif(alerte);
        return new AlerteDifficulteResponse(base.idAlerte(), base.idInscription(), base.idEleve(),
                base.nomEleve(), base.idClasse(), base.nomClasse(), base.trimestre(),
                base.moyenne(), base.ecart(), motif, base.dateAlerte());
    }

    /** Enrichit une alerte avec les données de référence, sans motif calculé. */
    private AlerteDifficulteResponse toResponseSansMotif(AlerteDifficulte alerte) {
        Inscription inscription = inscriptionRepository.findById(alerte.getIdInscription()).orElse(null);
        Eleve eleve = inscription == null ? null
                : eleveRepository.findById(inscription.getIdEleve()).orElse(null);
        Classe classe = inscription == null ? null
                : classeRepository.findById(inscription.getIdClasse()).orElse(null);
        return new AlerteDifficulteResponse(alerte.getIdAlerte(), alerte.getIdInscription(),
                eleve == null ? null : eleve.getIdEleve(), eleve == null ? null : eleve.getNom(),
                classe == null ? null : classe.getIdClasse(), classe == null ? null : classe.getNom(),
                alerte.getNumeroTrimestre(), alerte.getMoyenne(), alerte.getEcart(), null,
                alerte.getDateAlerte());
    }
}

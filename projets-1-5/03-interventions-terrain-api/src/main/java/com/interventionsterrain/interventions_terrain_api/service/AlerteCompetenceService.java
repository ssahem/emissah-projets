package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.response.AlerteAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.AlerteCompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alertes de compétences proches de l'expiration (F16).
 *
 * COUCHE       : Service planifié
 * RÔLE         : produire une alerte par agence et marquer chaque compétence signalée via
 *                {@code date_alerte}, sans créer de table d'alerte (E-07).
 * FRÉQUENCE    : 01:00 UTC, via la propriété {@code interventions-terrain.alertes-competences.cron}.
 */
@Service
@Transactional(readOnly = true)
public class AlerteCompetenceService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Logger log = LoggerFactory.getLogger(AlerteCompetenceService.class);
    private static final int JOURS_ALERTE = 30;

    /** Accès en base à la table associée à obtention. */
    private final ObtentionCompetenceRepository obtentionRepository;
    /** Accès en base à la table associée à technicien. */
    private final TechnicienRepository technicienRepository;
    /** Accès en base à la table associée à equipe. */
    private final EquipeRepository equipeRepository;
    /** Accès en base à la table associée à agence. */
    private final AgenceRepository agenceRepository;
    /** Accès en base à la table associée à competence. */
    private final CompetenceRepository competenceRepository;
    /** Horloge applicative injectée : toute date produite par le service passe par elle. */
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    public AlerteCompetenceService(ObtentionCompetenceRepository obtentionRepository,
                                   TechnicienRepository technicienRepository,
                                   EquipeRepository equipeRepository, AgenceRepository agenceRepository,
                                   CompetenceRepository competenceRepository, Clock clock) {
        this.obtentionRepository = obtentionRepository;
        this.technicienRepository = technicienRepository;
        this.equipeRepository = equipeRepository;
        this.agenceRepository = agenceRepository;
        this.competenceRepository = competenceRepository;
        this.clock = clock;
    }

    /** Tâche nocturne : génère puis journalise les alertes, une ligne par agence. */
    @Scheduled(cron = "${interventions-terrain.alertes-competences.cron:0 0 1 * * *}", zone = "UTC")
    @Transactional
    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    public void genererAlertesPlanifiees() {
        List<AlerteAgenceResponse> alertes = genererAlertes();
        if (alertes.isEmpty()) {
            log.info("Alerte compétences : aucune compétence n'expire dans les {} prochains jours",
                    JOURS_ALERTE);
            return;
        }
        alertes.forEach(alerte -> log.warn("Alerte compétences — agence {} : {}",
                alerte.nomAgence(), alerte.lignes()));
    }

    /** Construit les alertes et marque les compétences traitées pour ne jamais les signaler deux fois. */
    @Transactional
    public List<AlerteAgenceResponse> genererAlertes() {
        LocalDate aujourdHui = LocalDate.now(clock);
        List<ObtentionCompetence> obtentions = obtentionRepository
                .findAllByDateExpirationBetweenAndDateAlerteIsNull(
                        aujourdHui, aujourdHui.plusDays(JOURS_ALERTE));
        if (obtentions.isEmpty()) {
            return List.of();
        }

        Map<Integer, List<AlerteCompetenceResponse>> parAgence = new LinkedHashMap<>();
        Map<Integer, String> nomsAgence = new LinkedHashMap<>();
        for (ObtentionCompetence obtention : obtentions) {
            Technicien technicien = technicienRepository.findById(obtention.getIdTechnicien())
                    .orElseThrow(() -> new IllegalStateException(
                            "Technicien introuvable pour l'alerte : " + obtention.getIdTechnicien()));
            Equipe equipe = equipeRepository.findById(technicien.getIdEquipe())
                    .orElseThrow(() -> new IllegalStateException(
                            "Équipe introuvable pour l'alerte : " + technicien.getIdEquipe()));
            Agence agence = agenceRepository.findById(equipe.getIdAgence())
                    .orElseThrow(() -> new IllegalStateException(
                            "Agence introuvable pour l'alerte : " + equipe.getIdAgence()));
            Competence competence = competenceRepository.findById(obtention.getIdCompetence())
                    .orElseThrow(() -> new IllegalStateException(
                            "Compétence introuvable pour l'alerte : " + obtention.getIdCompetence()));

            nomsAgence.put(agence.getIdAgence(), agence.getNom());
            parAgence.computeIfAbsent(agence.getIdAgence(), ignored -> new ArrayList<>()).add(
                    new AlerteCompetenceResponse(technicien.getIdTechnicien(),
                            technicien.getNom() + " " + technicien.getPrenom(),
                            competence.getLibelle(), obtention.getDateExpiration()));
            obtention.signaler(aujourdHui);
        }

        return parAgence.entrySet().stream()
                .map(e -> new AlerteAgenceResponse(e.getKey(), nomsAgence.get(e.getKey()),
                        e.getValue().stream()
                                .sorted(Comparator.comparing(AlerteCompetenceResponse::dateExpiration)
                                        .thenComparing(AlerteCompetenceResponse::nomTechnicien))
                                .toList()))
                .sorted(Comparator.comparing(AlerteAgenceResponse::nomAgence))
                .toList();
    }
}

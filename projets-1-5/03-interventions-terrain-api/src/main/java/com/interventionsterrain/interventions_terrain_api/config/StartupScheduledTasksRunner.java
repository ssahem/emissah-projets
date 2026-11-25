package com.interventionsterrain.interventions_terrain_api.config;

import com.interventionsterrain.interventions_terrain_api.service.AlerteCompetenceService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exécution au démarrage local des alertes de compétences (F16).
 *
 * COUCHE       : Configuration
 * RÔLE         : déclenche une fois la génération des alertes de compétences quand le profil local est actif.
 * DÉPEND DE    : AlerteCompetenceService.
 * UTILISÉ PAR  : le cycle de vie Spring, à l'événement ApplicationReadyEvent.
 * NE FAIT PAS  : de logique métier ni de seconde planification ; la tâche reste exécutée par @Scheduled.
 */
@Component
@Profile("local")
public class StartupScheduledTasksRunner {

    private final AlerteCompetenceService alerteCompetenceService;

    public StartupScheduledTasksRunner(AlerteCompetenceService alerteCompetenceService) {
        this.alerteCompetenceService = alerteCompetenceService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DÉCLENCHEUR
    // ═════════════════════════════════════════════════════════════════════════

    // ── executerAuDemarrage() ────────────────────────────────────────────────

    /**
     * Déclenche la génération des alertes une fois l'application prête, sans
     * dupliquer la règle métier déjà exécutée chaque nuit par le scheduleur.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void executerAuDemarrage() {
        alerteCompetenceService.genererAlertesPlanifiees();
    }
}

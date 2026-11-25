package com.flottevehicules.flotte_vehicules_api.config;

import com.flottevehicules.flotte_vehicules_api.service.AlerteService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exécution au démarrage local de la génération des alertes d'entretien (F16).
 *
 * COUCHE       : Configuration
 * RÔLE         : déclenche une fois la génération des alertes quand le profil local est actif.
 * DÉPEND DE    : AlerteService.
 * UTILISÉ PAR  : le cycle de vie Spring, à l'événement ApplicationReadyEvent.
 * NE FAIT PAS  : de logique métier ni de seconde planification ; la tâche reste exécutée par @Scheduled.
 */
@Component
@Profile("local")
public class StartupScheduledTasksRunner {

    private final AlerteService alerteService;

    public StartupScheduledTasksRunner(AlerteService alerteService) {
        this.alerteService = alerteService;
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
        alerteService.tacheQuotidienne();
    }
}

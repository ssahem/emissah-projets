package com.suiviscolaire.suivi_scolaire_api.config;

import com.suiviscolaire.suivi_scolaire_api.service.AlertesDifficulteService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exécution au démarrage local de la détection des difficultés (F16).
 *
 * COUCHE       : Configuration
 * RÔLE         : déclenche une fois la détection des difficultés quand le profil local est actif.
 * DÉPEND DE    : AlertesDifficulteService.
 * UTILISÉ PAR  : le cycle de vie Spring, à l'événement ApplicationReadyEvent.
 * NE FAIT PAS  : de logique métier ni de seconde planification ; la tâche reste exécutée par @Scheduled.
 */
@Component
@Profile("local")
public class StartupScheduledTasksRunner {

    private final AlertesDifficulteService alertesDifficulteService;

    public StartupScheduledTasksRunner(AlertesDifficulteService alertesDifficulteService) {
        this.alertesDifficulteService = alertesDifficulteService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DÉCLENCHEUR
    // ═════════════════════════════════════════════════════════════════════════

    // ── executerAuDemarrage() ────────────────────────────────────────────────

    /**
     * Déclenche la détection une fois l'application prête, sans dupliquer la
     * règle métier déjà exécutée chaque semaine par le scheduleur.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void executerAuDemarrage() {
        alertesDifficulteService.detecter();
    }
}

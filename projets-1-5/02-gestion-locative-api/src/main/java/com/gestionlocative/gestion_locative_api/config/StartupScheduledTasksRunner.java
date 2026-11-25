package com.gestionlocative.gestion_locative_api.config;

import com.gestionlocative.gestion_locative_api.service.BailClotureService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Exécution au démarrage local de la clôture automatique des baux échus (F16).
 *
 * COUCHE       : Configuration
 * RÔLE         : déclenche une fois la clôture des baux échus quand le profil local est actif.
 * DÉPEND DE    : BailClotureService.
 * UTILISÉ PAR  : le cycle de vie Spring, à l'événement ApplicationReadyEvent.
 * NE FAIT PAS  : de logique métier ni de seconde planification ; la tâche reste exécutée par @Scheduled.
 */
@Component
@Profile("local")
public class StartupScheduledTasksRunner {

    private final BailClotureService bailClotureService;

    public StartupScheduledTasksRunner(BailClotureService bailClotureService) {
        this.bailClotureService = bailClotureService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DÉCLENCHEUR
    // ═════════════════════════════════════════════════════════════════════════

    // ── executerAuDemarrage() ────────────────────────────────────────────────

    /**
     * Déclenche la clôture des baux échus une fois l'application prête, sans
     * dupliquer la règle métier déjà exécutée chaque nuit par le scheduleur.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void executerAuDemarrage() {
        bailClotureService.cloturerBauxEchus();
    }
}

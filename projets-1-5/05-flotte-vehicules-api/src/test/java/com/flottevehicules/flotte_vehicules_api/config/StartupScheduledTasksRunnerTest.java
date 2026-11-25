package com.flottevehicules.flotte_vehicules_api.config;

import static org.mockito.Mockito.verify;

import com.flottevehicules.flotte_vehicules_api.service.AlerteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du déclenchement au démarrage local (profil local).
 *
 * COUCHE       : Test
 * RÔLE         : vérifie que l'événement de démarrage appelle la tâche sans la dupliquer.
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : service simulé, runner instancié directement, sans contexte Spring.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : de démarrage réel ni d'accès base.
 */
@ExtendWith(MockitoExtension.class)
class StartupScheduledTasksRunnerTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private AlerteService alerteService;

    @InjectMocks private StartupScheduledTasksRunner runner;

    // ── executerAuDemarrage_delegueUneSeuleFois() ────────────────────────────

    @Test
    @DisplayName("Le démarrage local déclenche une seule fois la génération des alertes")
    void executerAuDemarrage_delegueUneSeuleFois() {
        // ── Action ───────────────────────────────────────────────────────────
        runner.executerAuDemarrage();

        // ── Vérification ─────────────────────────────────────────────────────
        verify(alerteService).tacheQuotidienne();
    }
}

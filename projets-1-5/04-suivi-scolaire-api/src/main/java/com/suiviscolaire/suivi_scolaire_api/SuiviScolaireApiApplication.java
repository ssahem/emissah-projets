package com.suiviscolaire.suivi_scolaire_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'API de suivi scolaire.
 *
 * COUCHE       : Application
 * RÔLE         : démarrer le contexte Spring ; les tâches planifiées sont activées par
 *                SchedulingConfig.
 * NE FAIT PAS  : de logique métier ni de sécurité (l'énoncé ne prévoit aucun utilisateur).
 */
@SpringBootApplication
public class SuiviScolaireApiApplication {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    /** Lance l'application. */

    // ── main() ───────────────────────────────────────────────────────────────

    /**
     * Exécute main().
     */

    public static void main(String[] args) {
        SpringApplication.run(SuiviScolaireApiApplication.class, args);
    }
}

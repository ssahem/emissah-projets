package com.billetterie.billetterie_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'API de billetterie.
 *
 * COUCHE       : Application
 * RÔLE         : démarrer le contexte Spring et activer la tâche d'expiration (F16).
 * NE FAIT PAS  : de logique métier.
 */
@EnableScheduling
@SpringBootApplication
public class BilletterieApiApplication {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    /** Lance l'application. */

    // ── main() ───────────────────────────────────────────────────────────────

    /**
     * Exécute main().
     */

    public static void main(String[] args) {
        SpringApplication.run(BilletterieApiApplication.class, args);
    }
}

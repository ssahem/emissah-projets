package com.gestionlocative.gestion_locative_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'API de gestion locative.
 *
 * COUCHE       : Application
 * RÔLE         : démarrer le contexte Spring. L'activation des tâches planifiées est portée par
 *                {@code SchedulingConfig} (F16).
 * NE FAIT PAS  : de logique métier ni de sécurité (l'énoncé ne prévoit aucun utilisateur).
 */
@SpringBootApplication
public class GestionLocativeApiApplication {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════


    // ── main() ───────────────────────────────────────────────────────────────

    /**
     * Lance l'application.
     */

    public static void main(String[] args) {
        SpringApplication.run(GestionLocativeApiApplication.class, args);
    }
}

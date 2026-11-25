package com.flottevehicules.flotte_vehicules_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'API de la flotte de véhicules.
 *
 * COUCHE       : Application
 * RÔLE         : démarre le contexte Spring Boot.
 * SÉCURITÉ     : aucune authentification n'est configurée ni attendue par l'énoncé.
 * NE FAIT PAS  : de logique applicative.
 */
@SpringBootApplication
public class FlotteVehiculesApiApplication {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════


    // ── main() ───────────────────────────────────────────────────────────────

    /**
     * Lance l'application.
     */

    public static void main(String[] args) {
        SpringApplication.run(FlotteVehiculesApiApplication.class, args);
    }
}

package com.interventionsterrain.interventions_terrain_api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative.
 *
 * COUCHE       : Configuration
 * RÔLE         : fournir une unique source de temps pour F5, F7, F10, F11, F12, F13, F16 et
 *                F17.
 * NE FAIT PAS  : de logique métier ; les tests fournissent un bean fixe {@code @Primary}.
 */
@Configuration
public class ClockConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    /** Fournit l'horloge système UTC utilisée par les services temporels. */

    // ── clock() ──────────────────────────────────────────────────────────────

    /**
     * Exécute clock().
     */

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

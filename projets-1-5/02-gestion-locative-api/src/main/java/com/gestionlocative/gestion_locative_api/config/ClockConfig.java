package com.gestionlocative.gestion_locative_api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative.
 *
 * COUCHE       : Configuration
 * RÔLE         : fournir une unique source de temps pour F5, F12 à F19.
 * DÉPEND DE    : aucune.
 * NE FAIT PAS  : de logique métier ; les tests fournissent un bean fixe {@code @Primary}.
 */
@Configuration
public class ClockConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    // ── clock() ──────────────────────────────────────────────────────────────

    /**
     * Fournit l'horloge système UTC utilisée par les services temporels.
     */

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

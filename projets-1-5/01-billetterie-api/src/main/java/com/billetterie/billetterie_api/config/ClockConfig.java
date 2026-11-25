package com.billetterie.billetterie_api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative.
 *
 * COUCHE       : Configuration
 * RÔLE         : fournir une unique source de temps pour F13, F14, F16.
 * DÉPEND DE    : aucune.
 * NE FAIT PAS  : de logique métier ; les tests peuvent remplacer ce bean.
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

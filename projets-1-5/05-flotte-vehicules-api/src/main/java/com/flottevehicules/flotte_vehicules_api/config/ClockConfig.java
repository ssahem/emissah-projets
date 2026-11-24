package com.flottevehicules.flotte_vehicules_api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative partagée.
 *
 * COUCHE       : Configuration
 * RÔLE         : source de temps unique pour F2, F5, F6, F7, F13, F16 et F17.
 * NE FAIT PAS  : de logique métier ; les tests d'intégration fournissent un bean fixe
 *                @Primary.
 */
@Configuration
public class ClockConfig {

    /** Fournit l'horloge système UTC. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

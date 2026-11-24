package com.suiviscolaire.suivi_scolaire_api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge applicative.
 *
 * COUCHE       : Configuration
 * RÔLE         : fournir une source de temps unique pour la date de naissance, les clôtures et
 *                F16.
 * NE FAIT PAS  : de logique métier ; les tests fournissent un bean fixe @Primary.
 */
@Configuration
public class ClockConfig {

    /** Fournit l'horloge UTC utilisée par les services temporels. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

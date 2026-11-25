package com.flottevehicules.flotte_vehicules_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activation des tâches planifiées (F16).
 *
 * COUCHE       : Configuration
 * RÔLE         : rend actif le @Scheduled du service d'alertes.
 * NE FAIT PAS  : de logique métier ; sans cette annotation la tâche est ignorée.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

}

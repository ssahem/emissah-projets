package com.interventionsterrain.interventions_terrain_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activation des tâches planifiées.
 *
 * COUCHE       : Configuration
 * RÔLE         : rendre actifs les {@code @Scheduled} du service d'alertes de compétences
 *                (F16).
 * NE FAIT PAS  : de logique métier ; sans cette annotation, la tâche est silencieusement ignorée.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

}

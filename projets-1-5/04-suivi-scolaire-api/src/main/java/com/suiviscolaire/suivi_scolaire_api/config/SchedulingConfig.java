package com.suiviscolaire.suivi_scolaire_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activation des tâches planifiées.
 *
 * COUCHE       : Configuration
 * RÔLE         : rend actif le @Scheduled de détection des difficultés (F16).
 * NE FAIT PAS  : de logique métier ; sans cette annotation la tâche serait ignorée.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

package com.gestionlocative.gestion_locative_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activation des tâches planifiées.
 *
 * COUCHE       : Configuration
 * RÔLE         : rendre actifs les {@code @Scheduled} de {@code BailClotureService} (F16).
 * DÉPEND DE    : aucune.
 * NE FAIT PAS  : de logique métier ; sans cette annotation, la tâche est silencieusement ignorée.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

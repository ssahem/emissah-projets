package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/** Créneau d'un technicien, avec client et adresse de l'intervention (F10). */
public record PlanningInterventionResponse(
        Integer idIntervention,
        LocalDateTime debutPrevu,
        LocalDateTime finPrevue,
        String nomClient,
        String prenomClient,
        String voie,
        String commune,
        boolean annulee) {
}

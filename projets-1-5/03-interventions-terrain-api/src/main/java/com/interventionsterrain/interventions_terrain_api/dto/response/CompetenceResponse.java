package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Compétence renvoyée par l'API (F3). */
public record CompetenceResponse(
        Integer idCompetence,
        String libelle,
        String description) {
}

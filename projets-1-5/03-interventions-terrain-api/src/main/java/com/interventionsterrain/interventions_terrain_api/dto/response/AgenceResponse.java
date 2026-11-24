package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Agence renvoyée par l'API (F1). */
public record AgenceResponse(
        Integer idAgence,
        String nom,
        String adresse,
        String ville,
        String responsable) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Client renvoyé par l'API (F4). */
public record ClientResponse(
        Integer idClient,
        String nom,
        String prenom,
        String email,
        String telephone,
        String adresse,
        String commune) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Technicien renvoyé par l'API, avec l'agence déduite de son équipe (F2, A-01). */
public record TechnicienResponse(
        Integer idTechnicien,
        String nom,
        String prenom,
        String email,
        String telephone,
        String matricule,
        Integer idEquipe,
        Integer idAgence,
        String nomAgence) {
}

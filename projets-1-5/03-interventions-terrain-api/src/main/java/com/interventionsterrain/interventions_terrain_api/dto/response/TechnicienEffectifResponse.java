package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Technicien d'une agence avec son nombre de compétences valides (F6). */
public record TechnicienEffectifResponse(
        Integer idTechnicien,
        String nom,
        String prenom,
        String matricule,
        long nbCompetencesValides) {
}

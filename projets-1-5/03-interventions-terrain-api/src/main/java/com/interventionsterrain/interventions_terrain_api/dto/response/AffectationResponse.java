package com.interventionsterrain.interventions_terrain_api.dto.response;

/** Technicien affecté à une intervention (A-02). */
public record AffectationResponse(
        Integer idAffectation,
        Integer idIntervention,
        Integer idTechnicien,
        boolean estResponsable) {
}

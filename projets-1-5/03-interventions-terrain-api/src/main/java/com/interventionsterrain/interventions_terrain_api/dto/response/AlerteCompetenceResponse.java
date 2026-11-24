package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDate;

/** Une ligne d'alerte : technicien, compétence et date d'expiration (F16). */
public record AlerteCompetenceResponse(
        Integer idTechnicien,
        String nomTechnicien,
        String libelleCompetence,
        LocalDate dateExpiration) {
}

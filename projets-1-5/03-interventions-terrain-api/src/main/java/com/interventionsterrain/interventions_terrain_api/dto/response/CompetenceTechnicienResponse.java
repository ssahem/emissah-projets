package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDate;

/** Compétence obtenue par un technicien, expirée ou non (F7). */
public record CompetenceTechnicienResponse(
        Integer idCompetence,
        String libelle,
        LocalDate dateObtention,
        LocalDate dateExpiration,
        boolean expiree) {
}

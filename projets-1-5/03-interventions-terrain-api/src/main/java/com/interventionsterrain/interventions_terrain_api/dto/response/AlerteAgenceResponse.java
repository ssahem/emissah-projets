package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/** Alerte de compétences expirant bientôt, regroupée par agence (F16). */
public record AlerteAgenceResponse(
        Integer idAgence,
        String nomAgence,
        List<AlerteCompetenceResponse> lignes) {
}

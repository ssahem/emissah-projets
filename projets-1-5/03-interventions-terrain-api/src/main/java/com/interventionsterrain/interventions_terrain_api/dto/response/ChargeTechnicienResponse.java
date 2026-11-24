package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/** Charge d'un technicien sur la période demandée (F18). */
public record ChargeTechnicienResponse(
        Integer idTechnicien,
        String nom,
        String prenom,
        long nombreInterventions,
        BigDecimal heuresOccupees) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Charge d'une agence : détail par technicien et totaux (F18). */
public record ChargeAgenceResponse(
        Integer idAgence,
        String nomAgence,
        List<ChargeTechnicienResponse> techniciens,
        long nombreInterventionsTotal,
        BigDecimal heuresOccupeesTotales) {
}

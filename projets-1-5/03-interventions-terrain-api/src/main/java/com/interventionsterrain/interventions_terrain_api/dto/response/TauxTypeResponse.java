package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/** Taux de réussite d'un type d'intervention (F19). */
public record TauxTypeResponse(
        String codeType,
        long terminees,
        long echeouees,
        BigDecimal taux) {
}

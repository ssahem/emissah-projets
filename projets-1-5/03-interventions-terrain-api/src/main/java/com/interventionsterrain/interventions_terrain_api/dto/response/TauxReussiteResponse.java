package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Taux de réussite mensuel d'une agence, global et par type (F19). */
public record TauxReussiteResponse(
        Integer idAgence,
        String mois,
        long terminees,
        long echeouees,
        BigDecimal tauxGlobal,
        List<TauxTypeResponse> parType) {
}

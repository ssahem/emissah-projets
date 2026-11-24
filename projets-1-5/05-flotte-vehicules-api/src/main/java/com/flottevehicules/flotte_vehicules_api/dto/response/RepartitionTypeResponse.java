package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Coût des entretiens d'un type donné, pour la répartition demandée par F18. */
@Schema(description = "Répartition du coût des entretiens par type")
public record RepartitionTypeResponse(

        @Schema(description = "Libellé du type d'entretien", example = "Vidange périodique")
        String libelle,

        @Schema(description = "Coût cumulé du type", example = "920.00")
        BigDecimal cout) {
}

package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Kilométrage mensuel d'un site (F19). */
@Schema(description = "Total mensuel et détail par véhicule")
public record KilometrageSiteResponse(

        @Schema(description = "Site", example = "1")
        Integer idSite,

        @Schema(description = "Nom du site", example = "Paris Centre")
        String nomSite,

        @Schema(description = "Mois au format yyyy-MM", example = "2026-11")
        String mois,

        @Schema(description = "Total des kilomètres relevés", example = "19000")
        Integer kmTotal,

        @Schema(description = "Détail par véhicule")
        List<DetailKilometrageResponse> vehicules) {
}

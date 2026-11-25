package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Kilométrage mensuel d'un site (F19).
 *
 * COUCHE       : DTO
 * RÔLE         : Kilométrage mensuel d'un site (F19).
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Total mensuel et détail par véhicule")
public record KilometrageSiteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du site
        @Schema(description = "Site", example = "1")
        Integer idSite,
        // Nom du site courant
        @Schema(description = "Nom du site", example = "Paris Centre")
        String nomSite,
        // Mois
        @Schema(description = "Mois au format yyyy-MM", example = "2026-11")
        String mois,
        // Kilométrage total
        @Schema(description = "Total des kilomètres relevés", example = "19000")
        Integer kmTotal,
        // Véhicules
        @Schema(description = "Détail par véhicule")
        List<DetailKilometrageResponse> vehicules
) {

}

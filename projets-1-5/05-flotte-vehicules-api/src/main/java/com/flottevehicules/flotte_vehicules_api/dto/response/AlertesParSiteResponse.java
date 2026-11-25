package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Alertes regroupées par site, conformément à F16.
 *
 * COUCHE       : DTO
 * RÔLE         : Alertes regroupées par site, conformément à F16.
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Alertes d'un site")
public record AlertesParSiteResponse(

        @Schema(description = "Identifiant du site", example = "1")
        Integer idSite,

        @Schema(description = "Nom du site", example = "Paris Centre")
        String nomSite,

        @Schema(description = "Alertes du site")
        List<AlerteResponse> alertes) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

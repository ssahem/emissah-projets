package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Véhicules disponibles regroupés par site (F17).
 *
 * COUCHE       : DTO
 * RÔLE         : Véhicules disponibles regroupés par site (F17).
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Disponibilités d'un site sur une période")
public record DisponibilitesParSiteResponse(

        @Schema(description = "Site", example = "1")
        Integer idSite,

        @Schema(description = "Nom du site", example = "Paris Centre")
        String nomSite,

        @Schema(description = "Début de la période", example = "2026-11-23")
        LocalDate du,

        @Schema(description = "Fin de la période", example = "2026-11-29")
        LocalDate au,

        @Schema(description = "Véhicules disponibles")
        List<DisponibleVehiculeResponse> vehicules) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Détail du kilométrage d'un véhicule sur une restitution (F19).
 *
 * COUCHE       : DTO
 * RÔLE         : Détail du kilométrage d'un véhicule sur une restitution (F19).
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Kilométrage relevé pour un véhicule")
public record DetailKilometrageResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Kilométrage
        @Schema(description = "Kilomètres comptés sur le mois", example = "18500")
        Integer km
) {

}

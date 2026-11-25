package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Véhicule d'un site avec son état d'affectation (F6).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Véhicule d'un site avec son état d'affectation (F6).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : SiteService.
 * NE FAIT PAS  : d'accès base.
 */
@Schema(description = "Véhicule rattaché à un site et son état courant")
public record VehiculeSiteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Identifiant", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Marque
        @Schema(description = "Marque", example = "Peugeot")
        String marque,
        // Modèle
        @Schema(description = "Modèle", example = "308")
        String modele,
        // Etat Affectation
        @Schema(description = "État : LIBRE, AFFECTE ou IMMOBILISE", example = "AFFECTE")
        String etatAffectation,
        // Kilométrage actuel
        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel
) {

}

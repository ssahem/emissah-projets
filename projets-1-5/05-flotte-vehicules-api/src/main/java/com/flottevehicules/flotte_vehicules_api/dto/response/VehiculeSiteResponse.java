package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Véhicule d'un site avec son état d'affectation (F6).
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * PRODUIT PAR : SiteService.
 * NE FAIT PAS : d'accès base.
 */
@Schema(description = "Véhicule rattaché à un site et son état courant")
public record VehiculeSiteResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idVehicule,

        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,

        @Schema(description = "Marque", example = "Peugeot")
        String marque,

        @Schema(description = "Modèle", example = "308")
        String modele,

        @Schema(description = "État : LIBRE, AFFECTE ou IMMOBILISE", example = "AFFECTE")
        String etatAffectation,

        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel) {
}

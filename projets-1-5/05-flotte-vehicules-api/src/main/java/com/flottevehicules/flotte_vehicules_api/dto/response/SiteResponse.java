package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de sortie d'un site (F1).
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * PRODUIT PAR : SiteMapper.
 * NE FAIT PAS : d'accès base ni de logique métier.
 */
@Schema(description = "Site de l'entreprise")
public record SiteResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idSite,

        @Schema(description = "Nom unique", example = "Paris Centre")
        String nom,

        @Schema(description = "Ville", example = "Paris")
        String ville,

        @Schema(description = "Adresse", example = "12 rue de la Paix")
        String adresse) {
}

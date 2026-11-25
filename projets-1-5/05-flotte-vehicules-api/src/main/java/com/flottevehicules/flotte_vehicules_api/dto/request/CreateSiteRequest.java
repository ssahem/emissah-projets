package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de création d'un site (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un site (F1).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : les clients de l'API.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un site : nom, ville et adresse obligatoires, nom unique")
public record CreateSiteRequest(

        @Schema(description = "Nom unique du site", example = "Paris Centre")
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @Schema(description = "Ville du site", example = "Paris")
        @NotBlank(message = "La ville est obligatoire")
        String ville,

        @Schema(description = "Adresse du site", example = "12 rue de la Paix")
        @NotBlank(message = "L'adresse est obligatoire")
        String adresse) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

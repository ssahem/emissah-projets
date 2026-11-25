package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'une agence (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une agence (F1).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : AgenceController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'une agence")
public record CreateAgenceRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Agence Paris Centre")
        @NotBlank
        @Size(max = 100)
        String nom,
        // Ville
        @Schema(description = "Ville", example = "Paris")
        @NotBlank
        @Size(max = 100)
        String ville,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue de la Paix")
        @Size(max = 255)
        String adresse,
        // Responsable
        @Schema(description = "Responsable", example = "Exemple")
        @Size(max = 100)
        String responsable
) {
}

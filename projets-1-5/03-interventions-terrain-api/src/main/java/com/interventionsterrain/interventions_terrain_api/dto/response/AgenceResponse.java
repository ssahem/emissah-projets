package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Agence renvoyée par l'API (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Agence renvoyée par l'API (F1).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AgenceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Agence renvoyée par l'API (F1)")
public record AgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue de la Paix")
        String adresse,
        // Ville
        @Schema(description = "Ville", example = "Paris")
        String ville,
        // Responsable
        @Schema(description = "Responsable", example = "Exemple")
        String responsable
) {
}

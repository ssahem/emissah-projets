package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Compétence renvoyée par l'API (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Compétence renvoyée par l'API (F3).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : CompetenceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Compétence renvoyée par l'API (F3)")
public record CompetenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la compétence
        @Schema(description = "Identifiant de la compétence", example = "1")
        Integer idCompetence,
        // Libellé
        @Schema(description = "Libellé", example = "Devoir")
        String libelle,
        // Description
        @Schema(description = "Description", example = "Exemple")
        String description
) {
}

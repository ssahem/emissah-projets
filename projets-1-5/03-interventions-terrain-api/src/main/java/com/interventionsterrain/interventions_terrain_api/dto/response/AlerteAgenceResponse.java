package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Alerte de compétences expirant bientôt, regroupée par agence (F16).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Alerte de compétences expirant bientôt, regroupée par agence (F16).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AlerteAgenceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Alerte de compétences expirant bientôt, regroupée par agence (F16)")
public record AlerteAgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Nom de l’agence
        @Schema(description = "Nom de l’agence", example = "Traore")
        String nomAgence,
        // Lignes
        @Schema(description = "Lignes", example = "Exemple")
        List<AlerteCompetenceResponse> lignes
) {
}

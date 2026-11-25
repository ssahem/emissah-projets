package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Taux de réussite mensuel d'une agence, global et par type (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Taux de réussite mensuel d'une agence, global et par type (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TauxReussiteMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Taux de réussite mensuel d'une agence, global et par type (F19)")
public record TauxReussiteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Mois
        @Schema(description = "Mois", example = "2026-11")
        String mois,
        // Terminées
        @Schema(description = "Terminées", example = "Exemple")
        long terminees,
        // Échouées
        @Schema(description = "Échouées", example = "Exemple")
        long echeouees,
        // Taux global
        @Schema(description = "Taux global", example = "12.34")
        BigDecimal tauxGlobal,
        // Par Type
        @Schema(description = "Par Type", example = "Exemple")
        List<TauxTypeResponse> parType
) {
}

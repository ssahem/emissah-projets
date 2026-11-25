package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Taux de réussite d'un type d'intervention (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Taux de réussite d'un type d'intervention (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TauxTypeMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Taux de réussite d'un type d'intervention (F19)")
public record TauxTypeResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Code du type
        @Schema(description = "Code du type", example = "6E")
        String codeType,
        // Terminées
        @Schema(description = "Terminées", example = "Exemple")
        long terminees,
        // Échouées
        @Schema(description = "Échouées", example = "Exemple")
        long echeouees,
        // Taux
        @Schema(description = "Taux", example = "12.34")
        BigDecimal taux
) {
}

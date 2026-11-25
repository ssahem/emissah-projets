package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Poids d'un type d'évaluation (F13).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Poids d'un type d'évaluation (F13).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PoidsMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Poids d'un type d'évaluation (F13)")
public record PoidsResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Code
        @Schema(description = "Code", example = "6E")
        String code,
        // Libellé
        @Schema(description = "Libellé", example = "Devoir")
        String libelle,
        // Poids
        @Schema(description = "Poids", example = "2")
        BigDecimal poids
) {
}

package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Détail du calcul de la moyenne générale, coefficient par coefficient (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Détail du calcul de la moyenne générale, coefficient par coefficient (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : DetailCoefficientMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Détail du calcul de la moyenne générale, coefficient par coefficient (F18)")
public record DetailCoefficientResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Libellé de la matière
        @Schema(description = "Libellé de la matière", example = "Devoir")
        String libelleMatiere,
        // Moyenne
        @Schema(description = "Moyenne", example = "12.34")
        BigDecimal moyenne,
        // Coefficient
        @Schema(description = "Coefficient", example = "4")
        BigDecimal coefficient,
        // Contribution
        @Schema(description = "Contribution", example = "12.34")
        BigDecimal contribution
) {

}

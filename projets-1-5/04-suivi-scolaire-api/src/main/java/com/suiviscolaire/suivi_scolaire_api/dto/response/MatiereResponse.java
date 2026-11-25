package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Matière renvoyée par l'API (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Matière renvoyée par l'API (F3).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : MatiereMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Matière renvoyée par l'API (F3)")
public record MatiereResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Libellé
        @Schema(description = "Libellé", example = "Devoir")
        String libelle,
        // Code
        @Schema(description = "Code", example = "6E")
        String code,
        // Coefficient
        @Schema(description = "Coefficient", example = "4")
        BigDecimal coefficient
) {

}

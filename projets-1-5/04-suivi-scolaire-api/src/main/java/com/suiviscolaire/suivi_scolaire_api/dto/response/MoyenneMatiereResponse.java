package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Moyenne d'un élève dans une matière (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Moyenne d'un élève dans une matière (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : MoyenneMatiereMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Moyenne d'un élève dans une matière (F17)")
public record MoyenneMatiereResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Libellé de la matière
        @Schema(description = "Libellé de la matière", example = "Devoir")
        String libelleMatiere,
        // Coefficient
        @Schema(description = "Coefficient", example = "4")
        BigDecimal coefficient,
        // Moyenne
        @Schema(description = "Moyenne", example = "12.34")
        BigDecimal moyenne,
        // Nombre notes
        @Schema(description = "Nombre notes", example = "Traore")
        int nombreNotes,
        // Evaluee
        @Schema(description = "Evaluee", example = "Exemple")
        boolean evaluee
) {

}

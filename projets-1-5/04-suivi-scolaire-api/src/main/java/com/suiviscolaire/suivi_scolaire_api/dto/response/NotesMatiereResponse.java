package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Notes groupées par matière (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Notes groupées par matière (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : NotesMatiereMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Notes groupées par matière (F7)")
public record NotesMatiereResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Libellé de la matière
        @Schema(description = "Libellé de la matière", example = "Devoir")
        String libelleMatiere,
        // Notes
        @Schema(description = "Notes", example = "Exemple")
        List<NoteResponse> notes
) {

}

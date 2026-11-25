package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Notes d'une matière dans une classe, groupées par élève (F8).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Notes d'une matière dans une classe, groupées par élève (F8).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : NotesClasseMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Notes d'une matière dans une classe, groupées par élève (F8)")
public record NotesClasseResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Moyenne de la classe
        @Schema(description = "Moyenne de la classe", example = "12.34")
        BigDecimal moyenneClasse,
        // Élèves
        @Schema(description = "Élèves", example = "Exemple")
        List<NotesEleveResponse> eleves
) {

}

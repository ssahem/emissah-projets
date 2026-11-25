package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Note renvoyée par l'API (F5, F7, F8, F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Note renvoyée par l'API (F5, F7, F8, F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : NoteMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Note renvoyée par l'API (F5, F7, F8, F10)")
public record NoteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la note
        @Schema(description = "Identifiant de la note", example = "1")
        Integer idNote,
        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Nom de l’élève
        @Schema(description = "Nom de l’élève", example = "Traore")
        String nomEleve,
        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere,
        // Libellé de la matière
        @Schema(description = "Libellé de la matière", example = "Devoir")
        String libelleMatiere,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Nom de la classe
        @Schema(description = "Nom de la classe", example = "6A")
        String nomClasse,
        // Valeur
        @Schema(description = "Valeur", example = "14.50")
        BigDecimal valeur,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Identifiant du type d’évaluation
        @Schema(description = "Identifiant du type d’évaluation", example = "1")
        Integer idTypeEval,
        // Libellé du type eval
        @Schema(description = "Libellé du type eval", example = "Devoir")
        String libelleTypeEval,
        // Poids
        @Schema(description = "Poids", example = "2")
        BigDecimal poids,
        // Date
        @Schema(description = "Date", example = "2026-11-24")
        LocalDate date
) {

}

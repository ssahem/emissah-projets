package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Corps de création d'une note (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une note (F5).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : NoteController avec @Valid.
 * NE FAIT PAS  : de logique métier ; la résolution élève/classe se fait en service (A-01).
 */
@Schema(description = "Création d'une note")
public record CreateNoteRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l'élève", example = "1")
        @NotNull
        Integer idEleve,
        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        @NotNull
        Integer idMatiere,
        // Valeur
        @Schema(description = "Valeur entre 0 et 20", example = "14.5")
        @NotNull
        @DecimalMin("0")
        @DecimalMax("20")
        BigDecimal valeur,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        @NotNull
        @Min(1)
        @Max(3)
        Integer trimestre,
        // Identifiant du type d’évaluation
        @Schema(description = "Identifiant du type d'évaluation", example = "1")
        @NotNull
        Integer idTypeEval
) {

}

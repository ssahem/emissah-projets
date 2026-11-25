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
        @Schema(description = "Identifiant de l'élève", example = "1") @NotNull Integer idEleve,
        @Schema(description = "Identifiant de la matière", example = "1") @NotNull Integer idMatiere,
        @Schema(description = "Valeur entre 0 et 20", example = "14.5")
        @NotNull @DecimalMin("0") @DecimalMax("20") BigDecimal valeur,
        @Schema(description = "Numéro de trimestre (1 à 3)", example = "2")
        @NotNull @Min(1) @Max(3) Integer trimestre,
        @Schema(description = "Identifiant du type d'évaluation", example = "1") @NotNull Integer idTypeEval) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

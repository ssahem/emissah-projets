package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Corps de modification d'une note : seule la valeur est modifiable (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une note : seule la valeur est modifiable (F5).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : NoteController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification de la valeur d'une note")
public record UpdateNoteRequest(
        @Schema(description = "Nouvelle valeur entre 0 et 20", example = "16")
        @NotNull @DecimalMin("0") @DecimalMax("20") BigDecimal valeur) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

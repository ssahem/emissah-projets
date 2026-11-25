package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Corps de modification d'une matière (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une matière (F3).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : MatiereController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification d'une matière")
public record UpdateMatiereRequest(
        @Schema(description = "Libellé", example = "Mathematiques") @NotBlank String libelle,
        @Schema(description = "Coefficient strictement positif", example = "5")
        @NotNull @DecimalMin(value = "0.01") BigDecimal coefficient) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

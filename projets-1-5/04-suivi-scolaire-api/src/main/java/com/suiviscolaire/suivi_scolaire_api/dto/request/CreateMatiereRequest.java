package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Corps de création d'une matière (F3).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : MatiereController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Création d'une matière")
public record CreateMatiereRequest(
        @Schema(description = "Libellé", example = "Mathematiques") @NotBlank String libelle,
        @Schema(description = "Code", example = "MATH") @NotBlank String code,
        @Schema(description = "Coefficient strictement positif", example = "4")
        @NotNull @DecimalMin(value = "0.01") BigDecimal coefficient) {
}

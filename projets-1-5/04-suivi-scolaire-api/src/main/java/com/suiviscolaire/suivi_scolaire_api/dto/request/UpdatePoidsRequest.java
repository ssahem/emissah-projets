package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Corps de modification du poids d'un type d'évaluation (F13).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : TypeEvaluationController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification du poids d'un type d'évaluation")
public record UpdatePoidsRequest(
        @Schema(description = "Poids strictement positif", example = "2.5")
        @NotNull @DecimalMin(value = "0.01") BigDecimal poids) {
}

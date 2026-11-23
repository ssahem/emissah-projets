package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corps de création d'une salle. COUCHE : DTO (request) · IMMUABLE : record. */
@Schema(description = "Création d'une salle")
public record CreateSalleRequest(
        @Schema(description = "Nom de la salle", example = "Grand Hall") @NotBlank String nom,
        @Schema(description = "Adresse postale", example = "12 rue des Arts") @NotBlank String adresse,
        @Schema(description = "Ville", example = "Abidjan") @NotBlank String ville,
        @Schema(description = "Capacité", example = "120") @NotNull @Positive Integer capacite) {
}

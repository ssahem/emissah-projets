package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Corps de modification d'une place. COUCHE : DTO (request) · IMMUABLE : record. */
@Schema(description = "Modification d'une place")
public record UpdatePlaceRequest(
        @Schema(description = "Identifiant de la salle", example = "1") @NotNull @Positive Integer idSalle,
        @Schema(description = "Rang", example = "A") @NotBlank @Size(max = 5) String rang,
        @Schema(description = "Numéro", example = "13") @NotNull @Positive Integer numero,
        @Schema(description = "Catégorie", example = "ORCHESTRE") @NotBlank @Size(max = 20) String categorie) {
}

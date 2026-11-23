package com.billetterie.billetterie_api.dto.request;

import com.billetterie.billetterie_api.enums.TypeSpectacle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** Corps de création d'un événement. COUCHE : DTO (request) · IMMUABLE : record. */
@Schema(description = "Création d'un événement")
public record CreateEvenementRequest(
        @Schema(description = "Titre du spectacle", example = "Concert Jazz") @NotBlank String titre,
        @Schema(description = "Type", example = "CONCERT") @NotNull TypeSpectacle type,
        @Schema(description = "Places numérotées", example = "true") @NotNull Boolean placementNumerote,
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00")
        @NotNull @Future LocalDateTime dateHeure,
        @Schema(description = "Identifiant de la salle", example = "1") @NotNull Integer idSalle) {
}

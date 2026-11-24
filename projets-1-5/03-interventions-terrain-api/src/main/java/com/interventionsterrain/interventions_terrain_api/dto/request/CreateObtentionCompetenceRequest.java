package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/** Certification d'une compétence pour un technicien (F11). */
@Schema(description = "Association d'une compétence à un technicien")
public record CreateObtentionCompetenceRequest(

    @Schema(description = "Identifiant de la compétence", example = "5")
    @NotNull @Positive Integer idCompetence,

    @Schema(description = "Date d'obtention", example = "2026-01-10")
    @NotNull LocalDate dateObtention,

    @Schema(description = "Date d'expiration, facultative", example = "2027-01-10")
    LocalDate dateExpiration) {
}

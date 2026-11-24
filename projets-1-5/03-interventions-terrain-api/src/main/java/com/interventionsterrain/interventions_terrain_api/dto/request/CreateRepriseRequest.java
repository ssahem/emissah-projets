package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** Nouveau créneau d'une intervention de reprise (F15). */
@Schema(description = "Reprogrammation d'une intervention échouée")
public record CreateRepriseRequest(

    @Schema(description = "Nouveau début prévu", example = "2026-12-10T09:00:00")
    @NotNull LocalDateTime debutPrevu,

    @Schema(description = "Nouvelle fin prévue", example = "2026-12-10T12:00:00")
    @NotNull LocalDateTime finPrevue) {
}

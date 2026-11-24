package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/** Corps de modification d'une intervention (F5) : créneau et technicien responsable. */
@Schema(description = "Modification d'une intervention")
public record UpdateInterventionRequest(

    @Schema(description = "Début du créneau prévu", example = "2026-12-02T09:00:00")
    @NotNull LocalDateTime debutPrevu,

    @Schema(description = "Fin du créneau prévu", example = "2026-12-02T12:00:00")
    @NotNull LocalDateTime finPrevue,

    @Schema(description = "Nouveau technicien responsable", example = "2")
    @NotNull @Positive Integer idTechnicienResponsable) {
}

package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de modification d'une compétence (F3). */
@Schema(description = "Modification d'une compétence")
public record UpdateCompetenceRequest(

    @Schema(description = "Libellé", example = "Raccordement fibre")
    @NotBlank @Size(max = 100) String libelle,

    @Schema(description = "Description (facultative)")
    String description) {
}

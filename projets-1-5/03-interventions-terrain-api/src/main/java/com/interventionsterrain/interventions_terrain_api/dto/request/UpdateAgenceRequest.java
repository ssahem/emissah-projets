package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de modification d'une agence (F1). La ville est figée dès qu'une équipe est rattachée. */
@Schema(description = "Modification d'une agence")
public record UpdateAgenceRequest(

    @Schema(description = "Nom", example = "Agence Paris Centre")
    @NotBlank @Size(max = 100) String nom,

    @Schema(description = "Ville", example = "Paris")
    @NotBlank @Size(max = 100) String ville,

    @Schema(description = "Responsable (facultatif)")
    @Size(max = 100) String responsable) {
}

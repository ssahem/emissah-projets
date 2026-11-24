package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de création d'une agence (F1). */
@Schema(description = "Création d'une agence")
public record CreateAgenceRequest(

    @Schema(description = "Nom", example = "Agence Paris Centre")
    @NotBlank @Size(max = 100) String nom,

    @Schema(description = "Ville", example = "Paris")
    @NotBlank @Size(max = 100) String ville,

    @Schema(description = "Adresse (facultative : valeur par défaut si absente)")
    @Size(max = 255) String adresse,

    @Schema(description = "Responsable (facultatif)")
    @Size(max = 100) String responsable) {
}

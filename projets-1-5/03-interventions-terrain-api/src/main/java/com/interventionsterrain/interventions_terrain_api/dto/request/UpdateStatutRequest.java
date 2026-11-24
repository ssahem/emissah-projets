package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Nouveau statut demandé pour une intervention (F14). */
@Schema(description = "Changement de statut d'une intervention")
public record UpdateStatutRequest(

    @Schema(description = "Statut cible", example = "EN_COURS")
    @NotBlank String statut) {
}

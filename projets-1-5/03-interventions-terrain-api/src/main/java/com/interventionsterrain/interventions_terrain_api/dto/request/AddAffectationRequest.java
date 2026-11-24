package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corps d'ajout d'un technicien à une intervention (A-02). */
@Schema(description = "Affectation d'un technicien")
public record AddAffectationRequest(

    @Schema(description = "Identifiant du technicien", example = "2")
    @NotNull @Positive Integer idTechnicien,

    @Schema(description = "Promouvoir ce technicien comme responsable ; un seul responsable autorisé",
            example = "false")
    Boolean estResponsable) {

    /** Le défaut est un technicien non responsable. */
    public boolean responsable() {
        return Boolean.TRUE.equals(estResponsable);
    }
}

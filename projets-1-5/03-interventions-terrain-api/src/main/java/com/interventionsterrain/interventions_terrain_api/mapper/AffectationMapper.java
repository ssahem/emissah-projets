package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.AffectationResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import org.springframework.stereotype.Component;

/** Conversion Affectation → AffectationResponse. */
@Component
public class AffectationMapper {

    public AffectationResponse toResponse(Affectation affectation) {
        return new AffectationResponse(
                affectation.getIdAffectation(),
                affectation.getIdIntervention(),
                affectation.getIdTechnicien(),
                affectation.isEstResponsable());
    }
}

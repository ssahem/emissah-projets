package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import org.springframework.stereotype.Component;

/** Conversion Agence → AgenceResponse. */
@Component
public class AgenceMapper {

    public AgenceResponse toResponse(Agence agence) {
        return new AgenceResponse(
                agence.getIdAgence(),
                agence.getNom(),
                agence.getAdresse(),
                agence.getVille(),
                agence.getResponsable());
    }
}

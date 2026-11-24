package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import org.springframework.stereotype.Component;

/** Conversion Compétence → CompetenceResponse. */
@Component
public class CompetenceMapper {

    public CompetenceResponse toResponse(Competence competence) {
        return new CompetenceResponse(
                competence.getIdCompetence(),
                competence.getLibelle(),
                competence.getDescription());
    }
}

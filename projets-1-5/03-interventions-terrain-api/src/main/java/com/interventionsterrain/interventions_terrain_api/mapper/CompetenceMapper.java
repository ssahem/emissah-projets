package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import org.springframework.stereotype.Component;

/**
 * Conversion Compétence → CompetenceResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : convertir Competence entre couches, sans accès base ni logique métier.
 * DÉPEND DE    : les entités et les DTO Competence.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : de logique métier ni de persistance.
 */
@Component
public class CompetenceMapper {

    public CompetenceResponse toResponse(Competence competence) {
        return new CompetenceResponse(
                competence.getIdCompetence(),
                competence.getLibelle(),
                competence.getDescription());
    }
}

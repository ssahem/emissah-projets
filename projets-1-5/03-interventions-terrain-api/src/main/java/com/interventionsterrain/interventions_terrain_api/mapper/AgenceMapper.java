package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import org.springframework.stereotype.Component;

/**
 * Conversion Agence → AgenceResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : convertir Agence entre couches, sans accès base ni logique métier.
 * DÉPEND DE    : les entités et les DTO Agence.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : de logique métier ni de persistance.
 */
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

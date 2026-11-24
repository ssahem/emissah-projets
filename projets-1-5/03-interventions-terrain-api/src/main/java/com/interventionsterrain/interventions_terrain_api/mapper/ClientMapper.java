package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.ClientResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import org.springframework.stereotype.Component;

/** Conversion Client → ClientResponse. */
@Component
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getIdClient(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getTelephone(),
                client.getAdresse(),
                client.getCommune());
    }
}

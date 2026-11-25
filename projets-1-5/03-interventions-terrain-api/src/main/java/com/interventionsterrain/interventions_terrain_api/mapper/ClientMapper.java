package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.ClientResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import org.springframework.stereotype.Component;

/**
 * Conversion Client → ClientResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : convertir Client entre couches, sans accès base ni logique métier.
 * DÉPEND DE    : les entités et les DTO Client.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : de logique métier ni de persistance.
 */
@Component
public class ClientMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit un client en reprenant ses champs.
     */

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

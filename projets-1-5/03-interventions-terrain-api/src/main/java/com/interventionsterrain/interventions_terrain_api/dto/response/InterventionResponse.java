package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/** Intervention renvoyée par l'API (F5). */
public record InterventionResponse(
        Integer idIntervention,
        Integer idClient,
        String codeType,
        LocalDateTime dateDemande,
        LocalDateTime debutPrevu,
        LocalDateTime finPrevue,
        String voie,
        String commune,
        Integer idTechnicienResponsable) {
}

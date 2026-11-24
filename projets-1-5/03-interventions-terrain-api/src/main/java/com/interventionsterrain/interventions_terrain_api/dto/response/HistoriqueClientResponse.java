package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/** Intervention passée d'un client, avec responsable, type et statut (F8). */
public record HistoriqueClientResponse(
        Integer idIntervention,
        LocalDateTime dateDemande,
        LocalDateTime debutPrevu,
        LocalDateTime finPrevue,
        String codeType,
        Integer idTechnicienResponsable,
        String nomTechnicienResponsable,
        String statut) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/**
 * Intervention passée d'un client, avec responsable, type et statut (F8).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Intervention passée d'un client, avec responsable, type et statut (F8).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : HistoriqueClientService puis renvoyé par HistoriqueClientController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record HistoriqueClientResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idIntervention,
        LocalDateTime dateDemande,
        LocalDateTime debutPrevu,
        LocalDateTime finPrevue,
        String codeType,
        Integer idTechnicienResponsable,
        String nomTechnicienResponsable,
        String statut) {
}

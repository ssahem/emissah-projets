package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/**
 * Intervention renvoyée par l'API (F5).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : InterventionService puis renvoyé par InterventionController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record InterventionResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

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

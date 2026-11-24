package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDateTime;

/**
 * Créneau d'un technicien, avec client et adresse de l'intervention (F10).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : PlanningInterventionService puis renvoyé par PlanningInterventionController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record PlanningInterventionResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idIntervention,
        LocalDateTime debutPrevu,
        LocalDateTime finPrevue,
        String nomClient,
        String prenomClient,
        String voie,
        String commune,
        boolean annulee) {
}

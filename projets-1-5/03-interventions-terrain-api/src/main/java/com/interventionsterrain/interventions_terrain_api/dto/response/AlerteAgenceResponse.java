package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/**
 * Alerte de compétences expirant bientôt, regroupée par agence (F16).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Alerte de compétences expirant bientôt, regroupée par agence (F16).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AlerteAgenceService puis renvoyé par AlerteAgenceController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record AlerteAgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAgence,
        String nomAgence,
        List<AlerteCompetenceResponse> lignes) {
}

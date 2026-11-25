package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Compétence renvoyée par l'API (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Compétence renvoyée par l'API (F3).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : CompetenceService puis renvoyé par CompetenceController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record CompetenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idCompetence,
        String libelle,
        String description) {
}

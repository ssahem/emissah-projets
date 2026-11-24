package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Agence renvoyée par l'API (F1).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : AgenceService puis renvoyé par AgenceController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record AgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAgence,
        String nom,
        String adresse,
        String ville,
        String responsable) {
}

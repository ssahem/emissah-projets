package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Client renvoyé par l'API (F4).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : ClientService puis renvoyé par ClientController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record ClientResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idClient,
        String nom,
        String prenom,
        String email,
        String telephone,
        String adresse,
        String commune) {
}

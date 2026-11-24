package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Technicien renvoyé par l'API, avec l'agence déduite de son équipe (F2, A-01).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : TechnicienService puis renvoyé par TechnicienController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record TechnicienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idTechnicien,
        String nom,
        String prenom,
        String email,
        String telephone,
        String matricule,
        Integer idEquipe,
        Integer idAgence,
        String nomAgence) {
}

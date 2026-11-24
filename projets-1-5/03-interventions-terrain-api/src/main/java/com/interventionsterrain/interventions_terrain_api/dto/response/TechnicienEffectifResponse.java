package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Technicien d'une agence avec son nombre de compétences valides (F6).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : TechnicienEffectifService puis renvoyé par TechnicienEffectifController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record TechnicienEffectifResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idTechnicien,
        String nom,
        String prenom,
        String matricule,
        long nbCompetencesValides) {
}

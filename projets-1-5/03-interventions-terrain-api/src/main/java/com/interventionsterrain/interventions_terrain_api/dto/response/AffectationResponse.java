package com.interventionsterrain.interventions_terrain_api.dto.response;

/**
 * Technicien affecté à une intervention (A-02).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : AffectationService puis renvoyé par AffectationController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record AffectationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAffectation,
        Integer idIntervention,
        Integer idTechnicien,
        boolean estResponsable) {
}

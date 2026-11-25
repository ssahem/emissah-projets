package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/**
 * Charge d'un technicien sur la période demandée (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Charge d'un technicien sur la période demandée (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ChargeTechnicienService puis renvoyé par ChargeTechnicienController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record ChargeTechnicienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idTechnicien,
        String nom,
        String prenom,
        long nombreInterventions,
        BigDecimal heuresOccupees) {
}

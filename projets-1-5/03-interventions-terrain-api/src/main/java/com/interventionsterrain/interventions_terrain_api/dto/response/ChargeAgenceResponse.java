package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Charge d'une agence : détail par technicien et totaux (F18).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : ChargeAgenceService puis renvoyé par ChargeAgenceController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record ChargeAgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAgence,
        String nomAgence,
        List<ChargeTechnicienResponse> techniciens,
        long nombreInterventionsTotal,
        BigDecimal heuresOccupeesTotales) {
}

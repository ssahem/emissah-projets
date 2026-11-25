package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/**
 * Taux de réussite d'un type d'intervention (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Taux de réussite d'un type d'intervention (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TauxTypeService puis renvoyé par TauxTypeController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record TauxTypeResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        String codeType,
        long terminees,
        long echeouees,
        BigDecimal taux) {
}

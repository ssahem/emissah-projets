package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Taux de réussite mensuel d'une agence, global et par type (F19).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : TauxReussiteService puis renvoyé par TauxReussiteController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record TauxReussiteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAgence,
        String mois,
        long terminees,
        long echeouees,
        BigDecimal tauxGlobal,
        List<TauxTypeResponse> parType) {
}

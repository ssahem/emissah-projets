package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/**
 * Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : DisponiblesService puis renvoyé par DisponiblesController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record DisponiblesResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        List<AgenceDisponibleResponse> agences,
        String raisonFiltrage) {
}

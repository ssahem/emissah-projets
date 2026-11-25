package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : DisponiblesMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage")
public record DisponiblesResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Agences
        @Schema(description = "Agences", example = "Exemple")
        List<AgenceDisponibleResponse> agences,
        // Raison filtrage
        @Schema(description = "Raison filtrage", example = "Exemple")
        String raisonFiltrage
) {
}

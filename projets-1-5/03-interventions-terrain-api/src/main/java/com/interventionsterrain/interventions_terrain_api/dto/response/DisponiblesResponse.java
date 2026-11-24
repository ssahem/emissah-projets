package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/**
 * Résultat F17 : techniciens groupés par agence, ou liste vide et raison du filtrage.
 * Le champ {@code raisonFiltrage} est renseigné uniquement lorsqu'aucun technicien ne convient.
 */
public record DisponiblesResponse(
        List<AgenceDisponibleResponse> agences,
        String raisonFiltrage) {
}

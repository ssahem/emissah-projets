package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/**
 * Regroupement des techniciens disponibles par agence (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Regroupement des techniciens disponibles par agence (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AgenceDisponibleService puis renvoyé par AgenceDisponibleController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record AgenceDisponibleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idAgence,
        String nomAgence,
        List<TechnicienDisponibleResponse> techniciens) {
}

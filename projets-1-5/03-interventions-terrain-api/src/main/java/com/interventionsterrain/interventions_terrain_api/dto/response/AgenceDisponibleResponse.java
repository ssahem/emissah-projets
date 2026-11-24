package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.util.List;

/** Regroupement des techniciens disponibles par agence (F17). */
public record AgenceDisponibleResponse(
        Integer idAgence,
        String nomAgence,
        List<TechnicienDisponibleResponse> techniciens) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/** Technicien retenu par F17, avec sa charge d'interventions le jour demandé. */
public record TechnicienDisponibleResponse(
        Integer idTechnicien,
        String nom,
        String prenom,
        String matricule,
        BigDecimal chargeHeuresJour) {
}

package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.math.BigDecimal;

/**
 * Technicien retenu par F17, avec sa charge d'interventions le jour demandé.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Technicien retenu par F17, avec sa charge d'interventions le jour demandé.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TechnicienDisponibleService puis renvoyé par TechnicienDisponibleController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record TechnicienDisponibleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idTechnicien,
        String nom,
        String prenom,
        String matricule,
        BigDecimal chargeHeuresJour) {
}

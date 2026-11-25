package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Technicien retenu par F17, avec sa charge d'interventions le jour demandé.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Technicien retenu par F17, avec sa charge d'interventions le jour demandé.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TechnicienDisponibleMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Technicien retenu par F17, avec sa charge d'interventions le jour demandé")
public record TechnicienDisponibleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du technicien
        @Schema(description = "Identifiant du technicien", example = "1")
        Integer idTechnicien,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        String prenom,
        // Matricule
        @Schema(description = "Matricule", example = "MAT-001")
        String matricule,
        // Charge Heures jour
        @Schema(description = "Charge Heures jour", example = "12.50")
        BigDecimal chargeHeuresJour
) {
}

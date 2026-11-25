package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Charge d'un technicien sur la période demandée (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Charge d'un technicien sur la période demandée (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ChargeTechnicienMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Charge d'un technicien sur la période demandée (F18)")
public record ChargeTechnicienResponse(

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
        // Nombre interventions
        @Schema(description = "Nombre interventions", example = "Traore")
        long nombreInterventions,
        // Heures occupées
        @Schema(description = "Heures occupées", example = "12.50")
        BigDecimal heuresOccupees
) {
}

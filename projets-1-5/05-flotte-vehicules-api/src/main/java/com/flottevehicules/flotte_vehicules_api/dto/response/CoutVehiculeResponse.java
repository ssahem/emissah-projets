package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Coût total d'entretien d'un véhicule (F18, décision D-03).
 *
 * COUCHE       : DTO
 * RÔLE         : Coût total d'entretien d'un véhicule (F18, décision D-03).
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Coût des entretiens et coût au kilomètre")
public record CoutVehiculeResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Coût total
        @Schema(description = "Coût total des entretiens depuis la mise en service", example = "600.00")
        BigDecimal coutTotal,
        // Coût au Kilométrage
        @Schema(description = "Coût au kilomètre, zéro si compteur nul", example = "0.03")
        BigDecimal coutAuKm,
        // Kilométrage actuel
        @Schema(description = "Kilométrage courant", example = "19000")
        Integer kmActuel,
        // Age annees
        @Schema(description = "Âge en années", example = "2")
        Integer ageAnnees,
        // Repartition
        @Schema(description = "Répartition par type d'entretien")
        List<RepartitionTypeResponse> repartition
) {

}

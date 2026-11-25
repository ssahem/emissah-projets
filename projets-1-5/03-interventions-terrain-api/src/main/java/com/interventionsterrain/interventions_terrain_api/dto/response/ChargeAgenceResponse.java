package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Charge d'une agence : détail par technicien et totaux (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Charge d'une agence : détail par technicien et totaux (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ChargeAgenceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Charge d'une agence : détail par technicien et totaux (F18)")
public record ChargeAgenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Nom de l’agence
        @Schema(description = "Nom de l’agence", example = "Traore")
        String nomAgence,
        // Techniciens
        @Schema(description = "Techniciens", example = "Exemple")
        List<ChargeTechnicienResponse> techniciens,
        // Nombre interventions total
        @Schema(description = "Nombre interventions total", example = "Traore")
        long nombreInterventionsTotal,
        // Heures occupées totales
        @Schema(description = "Heures occupées totales", example = "12.50")
        BigDecimal heuresOccupeesTotales
) {
}

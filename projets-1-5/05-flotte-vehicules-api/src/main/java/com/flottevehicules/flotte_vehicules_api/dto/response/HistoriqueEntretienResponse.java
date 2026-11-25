package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Historique d'entretien d'un véhicule avec le coût cumulé (F8).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Historique d'entretien d'un véhicule avec le coût cumulé (F8).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EntretienService.
 * NE FAIT PAS  : d'accès base.
 */
@Schema(description = "Historique d'entretien et cumul des coûts")
public record HistoriqueEntretienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Coût cumule
        @Schema(description = "Coût cumulé total", example = "600.00")
        BigDecimal coutCumule,
        // Entretiens
        @Schema(description = "Entretiens triés du plus récent au plus ancien")
        List<EntretienResponse> entretiens
) {

}

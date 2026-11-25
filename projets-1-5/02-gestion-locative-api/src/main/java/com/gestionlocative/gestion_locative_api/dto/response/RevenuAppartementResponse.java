package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Détail du revenu d'un appartement sur un mois (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Détail du revenu d'un appartement sur un mois (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ImmeubleService, à partir des baux et de revision_loyer.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Revenu locatif d'un appartement sur un mois")
public record RevenuAppartementResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’appartement
        @Schema(description = "Identifiant", example = "1")
        Integer idAppartement,
        // Numéro
        @Schema(description = "Numéro", example = "1A")
        String numero,
        // Revenu
        @Schema(description = "Revenu de l'appartement", example = "425.00")
        BigDecimal revenu,
        // Jours occupes
        @Schema(description = "Jours occupés", example = "15")
        long joursOccupes,
        // Taux d’occupation
        @Schema(description = "Taux d'occupation en %", example = "50.00")
        BigDecimal tauxOccupation
) {
}

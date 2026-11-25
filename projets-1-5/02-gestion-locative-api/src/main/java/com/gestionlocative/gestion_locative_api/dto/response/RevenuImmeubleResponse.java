package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Revenu locatif d'un immeuble sur un mois (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Revenu locatif d'un immeuble sur un mois (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ImmeubleService.
 * MOIS         : chaîne au format AAAA-MM, telle que demandée par l'énoncé.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Revenu locatif d'un immeuble sur un mois")
public record RevenuImmeubleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’immeuble
        @Schema(description = "Identifiant", example = "1")
        Integer idImmeuble,
        // Mois
        @Schema(description = "Mois", example = "2026-11")
        String mois,
        // Revenu total
        @Schema(description = "Revenu total", example = "1700.00")
        BigDecimal revenuTotal,
        // Taux d’occupation
        @Schema(description = "Taux d'occupation en %", example = "75.00")
        BigDecimal tauxOccupation,
        // Detail
        @Schema(description = "Détail par appartement")
        List<RevenuAppartementResponse> detail
) {
}

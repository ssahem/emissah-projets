package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Revenu locatif d'un immeuble sur un mois (F19).
 *
 * COUCHE       : DTO (contrat public)
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

        // Identifiant de l'immeuble
        @Schema(description = "Identifiant", example = "1") Integer idImmeuble,
        // Mois au format AAAA-MM
        @Schema(description = "Mois", example = "2026-11") String mois,
        // Somme des revenus des appartements
        @Schema(description = "Revenu total", example = "1700.00") BigDecimal revenuTotal,
        // Jours occupés / (nombre d'appartements × jours du mois)
        @Schema(description = "Taux d'occupation en %", example = "75.00") BigDecimal tauxOccupation,
        // Un élément par appartement de l'immeuble
        @Schema(description = "Détail par appartement") List<RevenuAppartementResponse> detail) {
}

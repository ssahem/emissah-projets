package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Remplissage d'une catégorie de places (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Remplissage d'une catégorie de places (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RemplissageParCategorieMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Remplissage des places d'une catégorie")
public record RemplissageParCategorieResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE")
        String categorie,
        // Total
        @Schema(description = "Nombre total de places", example = "10")
        long total,
        // Provisoires
        @Schema(description = "Réservations provisoires", example = "2")
        long provisoires,
        // Payées
        @Schema(description = "Réservations payées", example = "3")
        long payees,
        // Taux
        @Schema(description = "Taux de remplissage en pourcentage", example = "50.00")
        BigDecimal taux
) {
}

package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Taux de remplissage d'un événement (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Taux de remplissage d'un événement (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RemplissageMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Taux de remplissage d'un événement")
public record RemplissageResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’evenement
        @Schema(description = "Identifiant de l'événement", example = "1")
        Integer idEvenement,
        // Places totales
        @Schema(description = "Nombre total de places", example = "120")
        long placesTotales,
        // Provisoires
        @Schema(description = "Réservations provisoires", example = "5")
        long provisoires,
        // Payées
        @Schema(description = "Réservations payées", example = "20")
        long payees,
        // Taux
        @Schema(description = "Taux de remplissage en pourcentage", example = "20.83")
        BigDecimal taux,
        // Par Catégorie
        @Schema(description = "Détail par catégorie")
        List<RemplissageParCategorieResponse> parCategorie
) {
}

package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** Remplissage d'une catégorie de places (F18). COUCHE : DTO (réponse). */
@Schema(description = "Remplissage des places d'une catégorie")
public record RemplissageParCategorieResponse(
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie,
        @Schema(description = "Nombre total de places", example = "10") long total,
        @Schema(description = "Réservations provisoires", example = "2") long provisoires,
        @Schema(description = "Réservations payées", example = "3") long payees,
        @Schema(description = "Taux de remplissage en pourcentage", example = "50.00") BigDecimal taux) {
}

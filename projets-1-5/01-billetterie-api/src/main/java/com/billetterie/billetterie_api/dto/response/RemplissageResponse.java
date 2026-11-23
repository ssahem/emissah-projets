package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Taux de remplissage d'un événement (F18). COUCHE : DTO (réponse). */
@Schema(description = "Taux de remplissage d'un événement")
public record RemplissageResponse(
        @Schema(description = "Identifiant de l'événement", example = "1") Integer idEvenement,
        @Schema(description = "Nombre total de places", example = "120") long placesTotales,
        @Schema(description = "Réservations provisoires", example = "5") long provisoires,
        @Schema(description = "Réservations payées", example = "20") long payees,
        @Schema(description = "Taux de remplissage en pourcentage", example = "20.83") BigDecimal taux,
        @Schema(description = "Détail par catégorie") List<RemplissageParCategorieResponse> parCategorie) {
}

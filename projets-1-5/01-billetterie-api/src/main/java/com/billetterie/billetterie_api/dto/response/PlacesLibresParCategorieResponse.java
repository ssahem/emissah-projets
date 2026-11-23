package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Groupe de places libres d'une catégorie (F17). COUCHE : DTO (réponse). */
@Schema(description = "Places libres d'une catégorie")
public record PlacesLibresParCategorieResponse(
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie,
        @Schema(description = "Nombre de places libres", example = "18") long placesLibres,
        @Schema(description = "Nombre de places occupées", example = "2") long placesOccupees) {
}

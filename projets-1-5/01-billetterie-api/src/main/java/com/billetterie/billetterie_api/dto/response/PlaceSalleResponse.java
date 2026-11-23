package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Place d'une salle (F5, F6). COUCHE : DTO (réponse) · PRODUIT PAR : PlaceMapper. */
@Schema(description = "Place d'une salle")
public record PlaceSalleResponse(
        @Schema(description = "Identifiant", example = "1") Integer idPlace,
        @Schema(description = "Rang", example = "A") String rang,
        @Schema(description = "Numéro", example = "12") Integer numero,
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie) {
}

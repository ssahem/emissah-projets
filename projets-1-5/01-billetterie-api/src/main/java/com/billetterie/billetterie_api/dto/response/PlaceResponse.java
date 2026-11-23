package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Place complète (F2). COUCHE : DTO (réponse) · PRODUIT PAR : PlaceMapper. */
@Schema(description = "Place d'une salle, avec son identifiant et sa salle")
public record PlaceResponse(
        @Schema(description = "Identifiant de la place", example = "1") Integer idPlace,
        @Schema(description = "Identifiant de la salle", example = "1") Integer idSalle,
        @Schema(description = "Rang", example = "A") String rang,
        @Schema(description = "Numéro", example = "13") Integer numero,
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie) {
}

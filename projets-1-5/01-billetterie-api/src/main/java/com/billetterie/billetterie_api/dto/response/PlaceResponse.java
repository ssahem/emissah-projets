package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Place complète (F2).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PlaceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Place d'une salle, avec son identifiant et sa salle")
public record PlaceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la place
        @Schema(description = "Identifiant de la place", example = "1") Integer idPlace,
        // Identifiant de la salle
        @Schema(description = "Identifiant de la salle", example = "1") Integer idSalle,
        // Rang
        @Schema(description = "Rang", example = "A") String rang,
        // Numéro
        @Schema(description = "Numéro", example = "13") Integer numero,
        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie) {
}

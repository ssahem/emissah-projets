package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Groupe de places libres d'une catégorie (F17).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PlacesLibresParCategorieMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Places libres d'une catégorie")
public record PlacesLibresParCategorieResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE") String categorie,
        // Nombre de places libres
        @Schema(description = "Nombre de places libres", example = "18") long placesLibres,
        // Nombre de places occupées
        @Schema(description = "Nombre de places occupées", example = "2") long placesOccupees) {
}

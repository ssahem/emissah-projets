package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Groupe de places libres d'une catégorie (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Groupe de places libres d'une catégorie (F17).
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
        @Schema(description = "Catégorie", example = "ORCHESTRE")
        String categorie,
        // Places libres
        @Schema(description = "Nombre de places libres", example = "18")
        long placesLibres,
        // Places occupées
        @Schema(description = "Nombre de places occupées", example = "2")
        long placesOccupees
) {
}

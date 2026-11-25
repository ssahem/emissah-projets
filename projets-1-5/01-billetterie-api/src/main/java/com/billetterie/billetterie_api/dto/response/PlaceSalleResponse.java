package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Place d'une salle (F5, F6).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Place d'une salle (F5, F6).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PlaceSalleMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Place d'une salle")
public record PlaceSalleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la place
        @Schema(description = "Identifiant", example = "1")
        Integer idPlace,
        // Rang
        @Schema(description = "Rang", example = "A")
        String rang,
        // Numéro
        @Schema(description = "Numéro", example = "12")
        Integer numero,
        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE")
        String categorie
) {
}

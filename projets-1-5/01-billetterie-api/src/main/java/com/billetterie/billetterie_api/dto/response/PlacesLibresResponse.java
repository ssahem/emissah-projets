package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Restitution des places libres d'un événement (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Restitution des places libres d'un événement (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PlacesLibresMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Places libres d'un événement")
public record PlacesLibresResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’evenement
        @Schema(description = "Identifiant de l'événement", example = "1")
        Integer idEvenement,
        // Places totales
        @Schema(description = "Nombre total de places", example = "120")
        long placesTotales,
        // Par Catégorie
        @Schema(description = "Détail par catégorie")
        List<PlacesLibresParCategorieResponse> parCategorie
) {
}

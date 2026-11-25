package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Salle renvoyée par l'API.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Salle renvoyée par l'API.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : SalleMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Salle de spectacle")
public record SalleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idSalle,
        // Nom
        @Schema(description = "Nom", example = "Grand Hall") String nom,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue des Arts") String adresse,
        // Ville
        @Schema(description = "Ville", example = "Abidjan") String ville,
        // Capacité
        @Schema(description = "Capacité", example = "120") Integer capacite) {
}

package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Salle renvoyée par l'API. COUCHE : DTO (réponse) · PRODUIT PAR : SalleMapper. */
@Schema(description = "Salle de spectacle")
public record SalleResponse(
        @Schema(description = "Identifiant", example = "1") Integer idSalle,
        @Schema(description = "Nom", example = "Grand Hall") String nom,
        @Schema(description = "Adresse", example = "12 rue des Arts") String adresse,
        @Schema(description = "Ville", example = "Abidjan") String ville,
        @Schema(description = "Capacité", example = "120") Integer capacite) {
}

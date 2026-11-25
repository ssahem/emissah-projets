package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Immeuble renvoyé par l'API (F2).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Immeuble renvoyé par l'API (F2).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ImmeubleMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Immeuble")
public record ImmeubleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idImmeuble,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue des Acacias") String adresse,
        // Code postal
        @Schema(description = "Code postal", example = "75001") String codePostal,
        // Ville
        @Schema(description = "Ville", example = "Paris") String ville,
        // Propriétaire
        @Schema(description = "Identifiant du propriétaire", example = "1") Integer idProprietaire,
        // Nombre d'étages
        @Schema(description = "Nombre d'étages", example = "3") Integer nbEtages,
        // Nombre d'appartements, exigé par la consultation (F2)
        @Schema(description = "Nombre d'appartements", example = "6") long nbAppartements) {
}

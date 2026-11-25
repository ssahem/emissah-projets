package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Client renvoyé par l'API (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Client renvoyé par l'API (F4).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ClientMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Client renvoyé par l'API (F4)")
public record ClientResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du client
        @Schema(description = "Identifiant du client", example = "1")
        Integer idClient,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        String prenom,
        // Email
        @Schema(description = "Email", example = "kofi@example.test")
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030405")
        String telephone,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue de la Paix")
        String adresse,
        // Commune
        @Schema(description = "Commune", example = "Exemple")
        String commune
) {
}

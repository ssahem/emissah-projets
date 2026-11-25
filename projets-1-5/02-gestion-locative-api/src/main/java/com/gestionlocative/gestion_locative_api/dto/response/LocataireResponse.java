package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Locataire renvoyé par l'API (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Locataire renvoyé par l'API (F4).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : LocataireMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Locataire")
public record LocataireResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du locataire
        @Schema(description = "Identifiant", example = "1")
        Integer idLocataire,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        String prenom,
        // Email
        @Schema(description = "Email", example = "kofi.traore@example.test")
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030406")
        String telephone
) {
}

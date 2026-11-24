package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Propriétaire renvoyé par l'API (F1).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ProprietaireMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Propriétaire")
public record ProprietaireResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idProprietaire,
        // Nom
        @Schema(description = "Nom", example = "Diallo") String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Awa") String prenom,
        // Email
        @Schema(description = "Email", example = "awa.diallo@example.test") String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030405") String telephone) {
}

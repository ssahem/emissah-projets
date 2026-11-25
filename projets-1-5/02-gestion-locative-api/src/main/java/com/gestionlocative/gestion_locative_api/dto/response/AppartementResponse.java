package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Appartement renvoyé par l'API (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Appartement renvoyé par l'API (F3).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AppartementMapper.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Appartement")
public record AppartementResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idAppartement,
        // Numéro
        @Schema(description = "Numéro", example = "1A") String numero,
        // Surface
        @Schema(description = "Surface en m²", example = "45.50") BigDecimal surface,
        // Nombre de pièces
        @Schema(description = "Nombre de pièces", example = "2") Integer nbPieces,
        // Étage
        @Schema(description = "Étage", example = "1") Integer etage,
        // Immeuble
        @Schema(description = "Identifiant de l'immeuble", example = "1") Integer idImmeuble) {
}

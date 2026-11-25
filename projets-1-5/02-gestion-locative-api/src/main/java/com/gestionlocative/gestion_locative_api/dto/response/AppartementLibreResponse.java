package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Appartement disponible à une date, avec son dernier loyer connu (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Appartement disponible à une date, avec son dernier loyer connu (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AppartementMapper, alimenté par AppartementService.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Appartement libre à une date")
public record AppartementLibreResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idAppartement,
        // Numéro
        @Schema(description = "Numéro", example = "1A") String numero,
        // Surface
        @Schema(description = "Surface en m²", example = "45.50") BigDecimal surface,
        // Dernier montant de revision_loyer, ou null si l'appartement n'a jamais été loué
        @Schema(description = "Dernier loyer connu", example = "850.00") BigDecimal dernierLoyer) {
}

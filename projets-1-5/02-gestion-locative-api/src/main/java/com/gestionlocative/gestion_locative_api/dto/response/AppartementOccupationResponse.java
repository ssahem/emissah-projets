package com.gestionlocative.gestion_locative_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Appartement d'un immeuble avec son état d'occupation (F6).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AppartementMapper, alimenté par ImmeubleService.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Appartement avec son état d'occupation")
public record AppartementOccupationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idAppartement,
        // Numéro
        @Schema(description = "Numéro", example = "1A") String numero,
        // Surface
        @Schema(description = "Surface en m²", example = "45.50") BigDecimal surface,
        // Étage
        @Schema(description = "Étage", example = "1") Integer etage,
        // Occupation à la date courante
        @Schema(description = "Occupé à la date courante", example = "true") boolean occupe,
        // LIBRE, A_VENIR ou LOUE
        @Schema(description = "État d'occupation", example = "LOUE") String statutOccupation) {
}

package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Corps de modification d'un appartement (F3).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * L'immeuble est figé : il ne figure pas dans ce contrat (F3).
 * UTILISÉ PAR  : AppartementController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un appartement")
public record UpdateAppartementRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Surface en m²
        @Schema(description = "Surface en m²", example = "48.00")
        @NotNull @DecimalMin(value = "0.01", message = "La surface doit être positive") BigDecimal surface,
        // Nombre de pièces
        @Schema(description = "Nombre de pièces", example = "3") @NotNull @Positive Integer nbPieces) {
}

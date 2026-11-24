package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Corps de création d'un appartement (F3).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : AppartementController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un appartement")
public record CreateAppartementRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Numéro, unique dans l'immeuble
        @Schema(description = "Numéro", example = "1A") @NotBlank @Size(max = 10) String numero,
        // Surface en m²
        @Schema(description = "Surface en m²", example = "45.50")
        @NotNull @DecimalMin(value = "0.01", message = "La surface doit être positive") BigDecimal surface,
        // Nombre de pièces
        @Schema(description = "Nombre de pièces", example = "2") @NotNull @Positive Integer nbPieces,
        // Étage (0 = rez-de-chaussée)
        @Schema(description = "Étage", example = "1") @NotNull @PositiveOrZero Integer etage,
        // Immeuble
        @Schema(description = "Identifiant de l'immeuble", example = "1")
        @NotNull @Positive Integer idImmeuble) {
}

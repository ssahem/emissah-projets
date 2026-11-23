package com.billetterie.billetterie_api.dto.request;

import com.billetterie.billetterie_api.enums.TypeSpectacle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Corps de modification d'un événement.
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Modification d'un événement")
public record UpdateEvenementRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Titre du spectacle
        @Schema(description = "Titre du spectacle", example = "Concert Jazz") @NotBlank @Size(max = 150) String titre,
        // Type
        @Schema(description = "Type", example = "CONCERT") @NotNull TypeSpectacle type,
        // Places numérotées
        @Schema(description = "Places numérotées", example = "true") @NotNull Boolean placementNumerote,
        // Date ISO-8601
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00")
        @NotNull @Future LocalDateTime dateHeure,
        // Identifiant de la salle
        @Schema(description = "Identifiant de la salle", example = "1") @NotNull @Positive Integer idSalle) {
}

package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de création d'une réservation.
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Création d'une réservation")
public record CreateReservationRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom du client
        @Schema(description = "Nom du client", example = "Dupont") @NotBlank String nom,
        // Prénom du client
        @Schema(description = "Prénom du client", example = "Jean") @NotBlank String prenom,
        // Email
        @Schema(description = "Email", example = "jean@example.com") @NotBlank @Email String email,
        // Identifiant de l'événement
        @Schema(description = "Identifiant de l'événement", example = "1") @NotNull Integer idEvenement,
        // Rang de la place
        @Schema(description = "Rang de la place", example = "A") @NotBlank String rang,
        // Numéro de la place
        @Schema(description = "Numéro de la place", example = "12") @NotNull @Positive Integer numero) {
}

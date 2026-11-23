package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de modification d'une réservation (nom, prénom, email seulement).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Modification des informations client")
public record UpdateReservationRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom du client
        @Schema(description = "Nom du client", example = "Dupont") @NotBlank String nom,
        // Prénom du client
        @Schema(description = "Prénom du client", example = "Jean") @NotBlank String prenom,
        // Email
        @Schema(description = "Email", example = "jean@example.com") @NotBlank @Email String email) {
}

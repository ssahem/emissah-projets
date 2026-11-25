package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'une réservation (nom, prénom, email seulement).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une réservation (nom, prénom, email seulement).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Modification des informations client")
public record UpdateReservationRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom du client", example = "Dupont")
        @NotBlank
        @Size(max = 100)
        String nom,
        // Prénom
        @Schema(description = "Prénom du client", example = "Jean")
        @NotBlank
        @Size(max = 100)
        String prenom,
        // Email
        @Schema(description = "Email", example = "jean@example.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {
}

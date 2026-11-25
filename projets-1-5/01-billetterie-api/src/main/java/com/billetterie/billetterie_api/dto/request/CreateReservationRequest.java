package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'une réservation.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une réservation.
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Création d'une réservation")
public record CreateReservationRequest(

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
        String email,
        // Identifiant de l’evenement
        @Schema(description = "Identifiant de l'événement", example = "1")
        @NotNull
        @Positive
        Integer idEvenement,
        // Rang
        @Schema(description = "Rang de la place", example = "A")
        @NotBlank
        @Size(max = 5)
        String rang,
        // Numéro
        @Schema(description = "Numéro de la place", example = "12")
        @NotNull
        @Positive
        Integer numero
) {
}

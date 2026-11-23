package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Corps de création d'une réservation. COUCHE : DTO (request) · IMMUABLE : record. */
@Schema(description = "Création d'une réservation")
public record CreateReservationRequest(
        @Schema(description = "Nom du client", example = "Dupont") @NotBlank String nom,
        @Schema(description = "Prénom du client", example = "Jean") @NotBlank String prenom,
        @Schema(description = "Email", example = "jean@example.com") @NotBlank @Email String email,
        @Schema(description = "Identifiant de l'événement", example = "1") @NotNull Integer idEvenement,
        @Schema(description = "Rang de la place", example = "A") @NotBlank String rang,
        @Schema(description = "Numéro de la place", example = "12") @NotNull @Positive Integer numero) {
}

package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corps de modification d'une réservation (nom, prénom, email seulement). COUCHE : DTO (request). */
@Schema(description = "Modification des informations client")
public record UpdateReservationRequest(
        @Schema(description = "Nom du client", example = "Dupont") @NotBlank String nom,
        @Schema(description = "Prénom du client", example = "Jean") @NotBlank String prenom,
        @Schema(description = "Email", example = "jean@example.com") @NotBlank @Email String email) {
}

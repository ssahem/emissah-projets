package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'un client (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un client (F4).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ClientController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un client")
public record CreateClientRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Dupont")
        @NotBlank
        @Size(max = 100)
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        @Size(max = 100)
        String prenom,
        // Email
        @Schema(description = "Email", example = "kofi@example.test")
        @Email
        @Size(max = 255)
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030407")
        @NotBlank
        @Size(max = 20)
        String telephone,
        // Adresse
        @Schema(description = "Adresse", example = "12 rue de la Paix")
        @NotBlank
        @Size(max = 255)
        String adresse,
        // Commune
        @Schema(description = "Commune", example = "Exemple")
        @NotBlank
        @Size(max = 100)
        String commune
) {
}

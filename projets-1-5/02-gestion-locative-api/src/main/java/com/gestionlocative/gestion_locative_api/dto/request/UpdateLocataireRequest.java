package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'un locataire (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un locataire (F4).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : LocataireController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un locataire")
public record UpdateLocataireRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Traore")
        @NotBlank
        @Size(max = 100)
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        @Size(max = 100)
        String prenom,
        // Email
        @Schema(description = "Email", example = "kofi.traore@example.test")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030406")
        @NotBlank
        @Size(max = 20)
        String telephone
) {
}

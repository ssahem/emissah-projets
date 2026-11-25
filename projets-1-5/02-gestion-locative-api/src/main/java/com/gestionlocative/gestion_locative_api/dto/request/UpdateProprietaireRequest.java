package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'un propriétaire (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un propriétaire (F1).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ProprietaireController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un propriétaire")
public record UpdateProprietaireRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Diallo")
        @NotBlank
        @Size(max = 100)
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Awa")
        @Size(max = 100)
        String prenom,
        // Email
        @Schema(description = "Email", example = "awa.diallo@example.test")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030405")
        @NotBlank
        @Size(max = 20)
        String telephone
) {
}

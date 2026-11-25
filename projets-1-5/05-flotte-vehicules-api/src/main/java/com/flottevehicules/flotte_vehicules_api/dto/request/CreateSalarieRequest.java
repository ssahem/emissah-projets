package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de création d'une fiche salarié (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une fiche salarié (F3).
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Création d'un salarié : nom, matricule et service obligatoires")
public record CreateSalarieRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Martin")
        @NotBlank(message = "Le nom est obligatoire")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Alice")
        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,
        // Email
        @Schema(description = "Adresse électronique unique", example = "alice.martin@example.test")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,
        // Matricule
        @Schema(description = "Matricule unique et figé", example = "MAT001")
        @NotBlank(message = "Le matricule est obligatoire")
        String matricule,
        // Service
        @Schema(description = "Service", example = "Direction")
        @NotBlank(message = "Le service est obligatoire")
        String service
) {

}

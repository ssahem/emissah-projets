package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'un technicien (F2). L'agence est obligatoire, l'équipe est déduite.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un technicien (F2). L'agence est obligatoire, l'équipe est déduite.
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : TechnicienController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un technicien")
public record CreateTechnicienRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Martin")
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
        @Schema(description = "Téléphone", example = "0102030405")
        @Size(max = 20)
        String telephone,
        // Identifiant de l’agence
        @Schema(description = "Identifiant de l'agence", example = "1")
        @NotNull
        @Positive
        Integer idAgence,
        // Identifiant de l’equipe
        @Schema(description = "Identifiant de l’equipe", example = "1")
        @Positive
        Integer idEquipe,
        // Matricule
        @Schema(description = "Matricule", example = "MAT001")
        @NotBlank
        @Size(max = 50)
        String matricule
) {
}

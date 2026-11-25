package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de modification d'une fiche salarié (F3) ; le matricule est figé.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une fiche salarié (F3) ; le matricule est figé.
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification d'un salarié ; le matricule n'est pas modifiable")
public record UpdateSalarieRequest(

        @Schema(description = "Nom", example = "Martin")
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @Schema(description = "Prénom", example = "Alice")
        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @Schema(description = "Adresse électronique unique", example = "alice.martin@example.test")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @Schema(description = "Nouveau service", example = "Technique")
        @NotBlank(message = "Le service est obligatoire")
        String service) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

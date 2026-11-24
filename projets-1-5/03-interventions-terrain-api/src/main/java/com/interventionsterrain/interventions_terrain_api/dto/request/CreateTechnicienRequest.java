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
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : TechnicienController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'un technicien")
public record CreateTechnicienRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Schema(description = "Nom", example = "Martin")
    @NotBlank @Size(max = 100) String nom,

    @Schema(description = "Prénom (facultatif)", example = "Camille")
    @Size(max = 100) String prenom,

    @Schema(description = "Email (facultatif : valeur unique générée si absent)")
    @Email @Size(max = 255) String email,

    @Schema(description = "Téléphone", example = "0102030405")
    @Size(max = 20) String telephone,

    @Schema(description = "Identifiant de l'agence", example = "1")
    @NotNull @Positive Integer idAgence,

    @Schema(description = "Identifiant d'une équipe précise de l'agence (facultatif)")
    @Positive Integer idEquipe,

    @Schema(description = "Matricule", example = "MAT001")
    @NotBlank @Size(max = 50) String matricule) {
}

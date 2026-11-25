package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de modification d'un professeur (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un professeur (F4).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ProfesseurController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification d'un professeur")
public record UpdateProfesseurRequest(
        @Schema(description = "Email unique", example = "claire.durand@example.test")
        @NotBlank @Email String email,
        @Schema(description = "Téléphone", example = "0102030401") String telephone,
        @Schema(description = "Identifiant de la matière principale", example = "1")
        @NotNull @Positive Integer idMatierePrincipale) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

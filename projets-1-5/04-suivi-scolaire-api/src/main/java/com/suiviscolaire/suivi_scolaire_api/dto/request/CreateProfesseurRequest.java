package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de création d'un professeur (F4, arbitrage A-02).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ProfesseurController avec @Valid.
 * NE FAIT PAS  : de logique métier ; la matière est enregistrée comme enseignement.
 */
@Schema(description = "Création d'un professeur")
public record CreateProfesseurRequest(
        @Schema(description = "Nom", example = "Durand") @NotBlank String nom,
        @Schema(description = "Prénom", example = "Claire") @NotBlank String prenom,
        @Schema(description = "Email unique", example = "claire.durand@example.test")
        @NotBlank @Email String email,
        @Schema(description = "Téléphone", example = "0102030401") String telephone,
        @Schema(description = "Identifiant de la matière principale", example = "1")
        @NotNull @Positive Integer idMatierePrincipale) {
}

package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de création d'une classe (F1).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ClasseController avec @Valid.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'une classe")
public record CreateClasseRequest(
        @Schema(description = "Nom de la classe", example = "6A") @NotBlank String nom,
        @Schema(description = "Identifiant de l'année scolaire", example = "2") @NotNull Integer idAnnee,
        @Schema(description = "Identifiant du niveau", example = "1") @NotNull Integer idNiveau,
        @Schema(description = "Effectif maximal", example = "30") @NotNull @Positive Integer effectifMax) {
}

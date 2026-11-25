package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Corps d'affectation d'une matière à un professeur dans une classe (F12).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps d'affectation d'une matière à un professeur dans une classe (F12).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ProfesseurController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Affectation d'une matière à un professeur dans une classe")
public record AffectationMatiereRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        @NotNull
        Integer idMatiere,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        @NotNull
        Integer idClasse
) {

}

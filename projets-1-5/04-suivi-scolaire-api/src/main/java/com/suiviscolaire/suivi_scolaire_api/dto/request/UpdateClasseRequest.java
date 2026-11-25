package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de modification d'une classe : seul l'effectif maximal est modifiable (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une classe : seul l'effectif maximal est modifiable (F1).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ClasseController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification d'une classe")
public record UpdateClasseRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Effectif maximal
        @Schema(description = "Nouvel effectif maximal", example = "32")
        @NotNull
        @Positive
        Integer effectifMax
) {

}

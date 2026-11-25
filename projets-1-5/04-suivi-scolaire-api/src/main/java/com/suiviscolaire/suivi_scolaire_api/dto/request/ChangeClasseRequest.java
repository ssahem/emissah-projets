package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de réaffectation d'un élève (F11).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de réaffectation d'un élève (F11).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : EleveController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Changement de classe d'un élève")
public record ChangeClasseRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du classedestination
        @Schema(description = "Identifiant de la classe de destination", example = "2")
        @NotNull
        Integer idClasseDestination
) {

}

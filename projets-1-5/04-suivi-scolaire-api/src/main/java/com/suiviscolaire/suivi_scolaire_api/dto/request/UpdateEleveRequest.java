package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de modification d'un élève ; la date de naissance est figée (F2).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un élève ; la date de naissance est figée (F2).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : EleveController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification d'un élève (date de naissance figée)")
public record UpdateEleveRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom
        @Schema(description = "Nom", example = "Kouassi")
        @NotBlank
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Aya")
        @NotBlank
        String prenom,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe de destination", example = "1")
        @NotNull
        Integer idClasse
) {

}

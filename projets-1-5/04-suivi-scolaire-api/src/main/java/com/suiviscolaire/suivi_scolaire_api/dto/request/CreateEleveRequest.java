package com.suiviscolaire.suivi_scolaire_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Corps de création d'une fiche élève (F2).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une fiche élève (F2).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : EleveController avec @Valid.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Création d'un élève")
public record CreateEleveRequest(

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
        // Date de naissance
        @Schema(description = "Date de naissance", example = "2011-02-14")
        @NotNull
        LocalDate dateNaissance,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        @NotNull
        Integer idClasse
) {

}

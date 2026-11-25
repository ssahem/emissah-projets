package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Résultat d'une affectation de matière à un professeur (F12).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat d'une affectation de matière à un professeur (F12).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AffectationMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Résultat d'une affectation de matière à un professeur (F12)")
public record AffectationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’enseignement
        @Schema(description = "Identifiant de l’enseignement", example = "1")
        Integer idEnseignement,
        // Identifiant du professeur
        @Schema(description = "Identifiant du professeur", example = "1")
        Integer idProfesseur,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Identifiant de la matière
        @Schema(description = "Identifiant de la matière", example = "1")
        Integer idMatiere
) {

}

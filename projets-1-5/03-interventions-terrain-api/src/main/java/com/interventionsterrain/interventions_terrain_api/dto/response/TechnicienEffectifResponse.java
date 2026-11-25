package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Technicien d'une agence avec son nombre de compétences valides (F6).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Technicien d'une agence avec son nombre de compétences valides (F6).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TechnicienEffectifMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Technicien d'une agence avec son nombre de compétences valides (F6)")
public record TechnicienEffectifResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du technicien
        @Schema(description = "Identifiant du technicien", example = "1")
        Integer idTechnicien,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Prénom
        @Schema(description = "Prénom", example = "Kofi")
        String prenom,
        // Matricule
        @Schema(description = "Matricule", example = "MAT-001")
        String matricule,
        // Nb compétences valides
        @Schema(description = "Nb compétences valides", example = "1")
        long nbCompetencesValides
) {
}

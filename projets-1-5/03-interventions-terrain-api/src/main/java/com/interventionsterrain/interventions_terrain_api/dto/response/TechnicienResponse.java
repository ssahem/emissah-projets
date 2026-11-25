package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Technicien renvoyé par l'API, avec l'agence déduite de son équipe (F2, A-01).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Technicien renvoyé par l'API, avec l'agence déduite de son équipe (F2, A-01).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : TechnicienMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Technicien renvoyé par l'API, avec l'agence déduite de son équipe (F2, A-01)")
public record TechnicienResponse(

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
        // Email
        @Schema(description = "Email", example = "kofi@example.test")
        String email,
        // Téléphone
        @Schema(description = "Téléphone", example = "0102030405")
        String telephone,
        // Matricule
        @Schema(description = "Matricule", example = "MAT-001")
        String matricule,
        // Identifiant de l’equipe
        @Schema(description = "Identifiant de l’equipe", example = "1")
        Integer idEquipe,
        // Identifiant de l’agence
        @Schema(description = "Identifiant de l’agence", example = "1")
        Integer idAgence,
        // Nom de l’agence
        @Schema(description = "Nom de l’agence", example = "Traore")
        String nomAgence
) {
}

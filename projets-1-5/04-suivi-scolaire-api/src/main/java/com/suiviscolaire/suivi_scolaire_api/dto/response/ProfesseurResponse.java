package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Professeur renvoyé par l'API, avec ses matières (F4, A-02).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Professeur renvoyé par l'API, avec ses matières (F4, A-02).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ProfesseurMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Professeur renvoyé par l'API, avec ses matières (F4, A-02)")
public record ProfesseurResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du professeur
        @Schema(description = "Identifiant du professeur", example = "1")
        Integer idProfesseur,
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
        // Matières
        @Schema(description = "Matières", example = "Exemple")
        List<Integer> matieres
) {

}

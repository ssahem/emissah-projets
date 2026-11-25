package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Classe renvoyée par l'API (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Classe renvoyée par l'API (F1).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ClasseMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Classe renvoyée par l'API (F1)")
public record ClasseResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Nom
        @Schema(description = "Nom", example = "Traore")
        String nom,
        // Identifiant de l’annee
        @Schema(description = "Identifiant de l’annee", example = "1")
        Integer idAnnee,
        // Identifiant du niveau
        @Schema(description = "Identifiant du niveau", example = "1")
        Integer idNiveau,
        // Effectif maximal
        @Schema(description = "Effectif maximal", example = "1")
        Integer effectifMax,
        // Effectif actuel
        @Schema(description = "Effectif actuel", example = "Exemple")
        long effectifActuel
) {

}

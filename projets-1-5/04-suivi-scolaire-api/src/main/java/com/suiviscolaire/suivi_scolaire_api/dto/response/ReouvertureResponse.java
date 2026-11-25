package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Trace de réouverture renvoyée par l'API (F15).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Trace de réouverture renvoyée par l'API (F15).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ReouvertureMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Trace de réouverture renvoyée par l'API (F15)")
public record ReouvertureResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du historique
        @Schema(description = "Identifiant du historique", example = "1")
        Integer idHistorique,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Date reouverture
        @Schema(description = "Date reouverture", example = "2026-11-24")
        LocalDate dateReouverture,
        // Motif
        @Schema(description = "Motif", example = "Exemple")
        String motif,
        // Auteur
        @Schema(description = "Auteur", example = "Exemple")
        String auteur
) {

}

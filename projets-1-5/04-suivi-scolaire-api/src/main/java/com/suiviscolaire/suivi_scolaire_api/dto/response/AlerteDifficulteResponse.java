package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alerte de difficulté renvoyée par l'API (F16).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Alerte de difficulté renvoyée par l'API (F16).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AlerteDifficulteMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Alerte de difficulté renvoyée par l'API (F16)")
public record AlerteDifficulteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’alerte
        @Schema(description = "Identifiant de l’alerte", example = "1")
        Integer idAlerte,
        // Identifiant de l’inscription
        @Schema(description = "Identifiant de l’inscription", example = "1")
        Integer idInscription,
        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Nom de l’élève
        @Schema(description = "Nom de l’élève", example = "Traore")
        String nomEleve,
        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Nom de la classe
        @Schema(description = "Nom de la classe", example = "6A")
        String nomClasse,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Moyenne
        @Schema(description = "Moyenne", example = "12.34")
        BigDecimal moyenne,
        // Ecart
        @Schema(description = "Ecart", example = "12.34")
        BigDecimal ecart,
        // Motif
        @Schema(description = "Motif", example = "Exemple")
        String motif,
        // Date alerte
        @Schema(description = "Date alerte", example = "2026-11-24")
        LocalDate dateAlerte
) {

}

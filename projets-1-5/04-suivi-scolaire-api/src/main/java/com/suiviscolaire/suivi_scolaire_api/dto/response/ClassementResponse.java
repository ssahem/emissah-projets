package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Classement d'une classe (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Classement d'une classe (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ClassementMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Classement d'une classe (F19)")
public record ClassementResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Moyenne de la classe
        @Schema(description = "Moyenne de la classe", example = "12.34")
        BigDecimal moyenneClasse,
        // Meilleure Moyenne
        @Schema(description = "Meilleure Moyenne", example = "12.34")
        BigDecimal meilleureMoyenne,
        // Plus faible Moyenne
        @Schema(description = "Plus faible Moyenne", example = "12.34")
        BigDecimal plusFaibleMoyenne,
        // Élèves
        @Schema(description = "Élèves", example = "Exemple")
        List<LigneClassementResponse> eleves
) {

}

package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Moyenne générale d'un élève et détail du calcul (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Moyenne générale d'un élève et détail du calcul (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : MoyenneGeneraleMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Moyenne générale d'un élève et détail du calcul (F18)")
public record MoyenneGeneraleResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Moyenne générale
        @Schema(description = "Moyenne générale", example = "12.34")
        BigDecimal moyenneGenerale,
        // Detail
        @Schema(description = "Detail", example = "Exemple")
        List<DetailCoefficientResponse> detail
) {

}

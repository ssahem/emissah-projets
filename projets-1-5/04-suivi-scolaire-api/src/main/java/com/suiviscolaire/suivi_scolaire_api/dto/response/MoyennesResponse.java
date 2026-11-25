package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Moyennes par matière d'un élève (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Moyennes par matière d'un élève (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : MoyennesMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Moyennes par matière d'un élève (F17)")
public record MoyennesResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’élève
        @Schema(description = "Identifiant de l’élève", example = "1")
        Integer idEleve,
        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Matières
        @Schema(description = "Matières", example = "Exemple")
        List<MoyenneMatiereResponse> matieres
) {

}

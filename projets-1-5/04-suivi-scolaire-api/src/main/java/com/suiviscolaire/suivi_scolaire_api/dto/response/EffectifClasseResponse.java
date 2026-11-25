package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Effectif et élèves d'une classe (F6).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Effectif et élèves d'une classe (F6).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EffectifClasseMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Effectif et élèves d'une classe (F6)")
public record EffectifClasseResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la classe
        @Schema(description = "Identifiant de la classe", example = "1")
        Integer idClasse,
        // Nom de la classe
        @Schema(description = "Nom de la classe", example = "6A")
        String nomClasse,
        // Effectif actuel
        @Schema(description = "Effectif actuel", example = "Exemple")
        long effectifActuel,
        // Effectif maximal
        @Schema(description = "Effectif maximal", example = "1")
        Integer effectifMax,
        // Élèves
        @Schema(description = "Élèves", example = "Exemple")
        List<EleveResponse> eleves
) {

}

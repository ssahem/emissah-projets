package com.suiviscolaire.suivi_scolaire_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Résultat de la clôture d'un trimestre (F14).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Résultat de la clôture d'un trimestre (F14).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ClotureMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Résultat de la clôture d'un trimestre (F14)")
public record ClotureResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Trimestre
        @Schema(description = "Trimestre", example = "1")
        Integer trimestre,
        // Date de clôture
        @Schema(description = "Date de clôture", example = "2026-11-24")
        LocalDate dateCloture,
        // Nombre enseignements verifies
        @Schema(description = "Nombre enseignements verifies", example = "Traore")
        int nombreEnseignementsVerifies
) {

}

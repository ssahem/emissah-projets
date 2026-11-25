package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Compétence obtenue par un technicien, expirée ou non (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Compétence obtenue par un technicien, expirée ou non (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : CompetenceTechnicienMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Compétence obtenue par un technicien, expirée ou non (F7)")
public record CompetenceTechnicienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la compétence
        @Schema(description = "Identifiant de la compétence", example = "1")
        Integer idCompetence,
        // Libellé
        @Schema(description = "Libellé", example = "Devoir")
        String libelle,
        // Date obtention
        @Schema(description = "Date obtention", example = "2026-11-24")
        LocalDate dateObtention,
        // Date expiration
        @Schema(description = "Date expiration", example = "2026-11-24")
        LocalDate dateExpiration,
        // Expiree
        @Schema(description = "Expiree", example = "Exemple")
        boolean expiree
) {
}

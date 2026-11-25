package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Une ligne d'alerte : technicien, compétence et date d'expiration (F16).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Une ligne d'alerte : technicien, compétence et date d'expiration (F16).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AlerteCompetenceMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Une ligne d'alerte : technicien, compétence et date d'expiration (F16)")
public record AlerteCompetenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant du technicien
        @Schema(description = "Identifiant du technicien", example = "1")
        Integer idTechnicien,
        // Nom technicien
        @Schema(description = "Nom technicien", example = "Traore")
        String nomTechnicien,
        // Libellé Compétence
        @Schema(description = "Libellé Compétence", example = "Devoir")
        String libelleCompetence,
        // Date expiration
        @Schema(description = "Date expiration", example = "2026-11-24")
        LocalDate dateExpiration
) {
}

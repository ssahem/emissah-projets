package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Intervention renvoyée par l'API (F5).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Intervention renvoyée par l'API (F5).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : InterventionMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Intervention renvoyée par l'API (F5)")
public record InterventionResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’intervention
        @Schema(description = "Identifiant de l’intervention", example = "1")
        Integer idIntervention,
        // Identifiant du client
        @Schema(description = "Identifiant du client", example = "1")
        Integer idClient,
        // Code du type
        @Schema(description = "Code du type", example = "6E")
        String codeType,
        // Date demande
        @Schema(description = "Date demande", example = "2026-11-24")
        LocalDateTime dateDemande,
        // Début prévu
        @Schema(description = "Début prévu", example = "Exemple")
        LocalDateTime debutPrevu,
        // Fin prévue
        @Schema(description = "Fin prévue", example = "Exemple")
        LocalDateTime finPrevue,
        // Voie
        @Schema(description = "Voie", example = "Exemple")
        String voie,
        // Commune
        @Schema(description = "Commune", example = "Exemple")
        String commune,
        // Identifiant du technicien responsable
        @Schema(description = "Identifiant du technicien responsable", example = "1")
        Integer idTechnicienResponsable
) {
}

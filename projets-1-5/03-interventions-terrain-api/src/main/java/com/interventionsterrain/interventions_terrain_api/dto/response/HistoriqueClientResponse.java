package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Intervention passée d'un client, avec responsable, type et statut (F8).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Intervention passée d'un client, avec responsable, type et statut (F8).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : HistoriqueClientMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Intervention passée d'un client, avec responsable, type et statut (F8)")
public record HistoriqueClientResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’intervention
        @Schema(description = "Identifiant de l’intervention", example = "1")
        Integer idIntervention,
        // Date demande
        @Schema(description = "Date demande", example = "2026-11-24")
        LocalDateTime dateDemande,
        // Début prévu
        @Schema(description = "Début prévu", example = "Exemple")
        LocalDateTime debutPrevu,
        // Fin prévue
        @Schema(description = "Fin prévue", example = "Exemple")
        LocalDateTime finPrevue,
        // Code du type
        @Schema(description = "Code du type", example = "6E")
        String codeType,
        // Identifiant du technicien responsable
        @Schema(description = "Identifiant du technicien responsable", example = "1")
        Integer idTechnicienResponsable,
        // Nom technicien Responsable
        @Schema(description = "Nom technicien Responsable", example = "Traore")
        String nomTechnicienResponsable,
        // Statut
        @Schema(description = "Statut", example = "Exemple")
        String statut
) {
}

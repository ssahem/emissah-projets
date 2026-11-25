package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Créneau d'un technicien, avec client et adresse de l'intervention (F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Créneau d'un technicien, avec client et adresse de l'intervention (F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : PlanningInterventionMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Créneau d'un technicien, avec client et adresse de l'intervention (F10)")
public record PlanningInterventionResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’intervention
        @Schema(description = "Identifiant de l’intervention", example = "1")
        Integer idIntervention,
        // Début prévu
        @Schema(description = "Début prévu", example = "Exemple")
        LocalDateTime debutPrevu,
        // Fin prévue
        @Schema(description = "Fin prévue", example = "Exemple")
        LocalDateTime finPrevue,
        // Nom client
        @Schema(description = "Nom client", example = "Traore")
        String nomClient,
        // Prénom client
        @Schema(description = "Prénom client", example = "Kofi")
        String prenomClient,
        // Voie
        @Schema(description = "Voie", example = "Exemple")
        String voie,
        // Commune
        @Schema(description = "Commune", example = "Exemple")
        String commune,
        // Annulee
        @Schema(description = "Annulee", example = "Exemple")
        boolean annulee
) {
}

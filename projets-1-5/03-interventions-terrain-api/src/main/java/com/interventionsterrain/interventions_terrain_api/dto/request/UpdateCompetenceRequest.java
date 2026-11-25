package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'une compétence (F3).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'une compétence (F3).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : CompetenceController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'une compétence")
public record UpdateCompetenceRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Libellé
        @Schema(description = "Libellé", example = "Raccordement fibre")
        @NotBlank
        @Size(max = 100)
        String libelle,
        // Description
        @Schema(description = "Description", example = "Exemple")
        String description
) {
}

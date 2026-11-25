package com.interventionsterrain.interventions_terrain_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Technicien affecté à une intervention (A-02).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Technicien affecté à une intervention (A-02).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : AffectationMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Technicien affecté à une intervention (A-02)")
public record AffectationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’affectation
        @Schema(description = "Identifiant de l’affectation", example = "1")
        Integer idAffectation,
        // Identifiant de l’intervention
        @Schema(description = "Identifiant de l’intervention", example = "1")
        Integer idIntervention,
        // Identifiant du technicien
        @Schema(description = "Identifiant du technicien", example = "1")
        Integer idTechnicien,
        // Est Responsable
        @Schema(description = "Est Responsable", example = "Exemple")
        boolean estResponsable
) {
}

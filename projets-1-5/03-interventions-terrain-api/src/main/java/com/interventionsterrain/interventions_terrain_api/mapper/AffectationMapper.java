package com.interventionsterrain.interventions_terrain_api.mapper;

import com.interventionsterrain.interventions_terrain_api.dto.response.AffectationResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import org.springframework.stereotype.Component;

/**
 * Conversion Affectation → AffectationResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : convertir Affectation entre couches, sans accès base ni logique métier.
 * DÉPEND DE    : les entités et les DTO Affectation.
 * UTILISÉ PAR  : les services.
 * NE FAIT PAS  : de logique métier ni de persistance.
 */
@Component
public class AffectationMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    public AffectationResponse toResponse(Affectation affectation) {
        return new AffectationResponse(
                affectation.getIdAffectation(),
                affectation.getIdIntervention(),
                affectation.getIdTechnicien(),
                affectation.isEstResponsable());
    }
}

package com.flottevehicules.flotte_vehicules_api.mapper;

import com.flottevehicules.flotte_vehicules_api.dto.response.SalarieResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import org.springframework.stereotype.Component;

/**
 * Conversion Salarie → SalarieResponse.
 *
 * COUCHE       : Mapper (interface HTTP)
 * RÔLE         : Conversion Salarie → SalarieResponse.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Component
public class SalarieMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Convertit une entité en réponse. */

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    public SalarieResponse toResponse(Salarie salarie) {
        return new SalarieResponse(
                salarie.getIdSalarie(), salarie.getNom(), salarie.getPrenom(),
                salarie.getEmail(), salarie.getMatricule(), salarie.getService());
    }
}

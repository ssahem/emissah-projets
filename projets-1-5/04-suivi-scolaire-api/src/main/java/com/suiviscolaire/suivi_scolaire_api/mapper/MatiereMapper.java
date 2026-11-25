package com.suiviscolaire.suivi_scolaire_api.mapper;

import com.suiviscolaire.suivi_scolaire_api.dto.response.MatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import org.springframework.stereotype.Component;

/**
 * Conversion Matière ↔ MatiereResponse.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire une entité en DTO sans logique métier.
 * NE FAIT PAS  : d'accès base ni de HTTP.
 */
@Component
public class MatiereMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════


    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit une matière en DTO.
     */

    public MatiereResponse toResponse(Matiere matiere) {
        return new MatiereResponse(matiere.getIdMatiere(), matiere.getLibelle(),
                matiere.getCode(), matiere.getCoefficient());
    }
}

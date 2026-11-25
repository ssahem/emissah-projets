package com.billetterie.billetterie_api.mapper;

import com.billetterie.billetterie_api.dto.response.SalleResponse;
import com.billetterie.billetterie_api.entity.Salle;
import org.springframework.stereotype.Component;

/**
 * Conversions entre les entités Salle et leurs DTO.
 *
 * COUCHE       : Mapper
 * RÔLE         : transformer une entité en réponse HTTP.
 * NE FAIT PAS  : de règle métier ni d'accès base.
 */
@Component
public class SalleMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Convertit une salle en réponse HTTP. */
    public SalleResponse toResponse(Salle salle) {
        return new SalleResponse(
                salle.getIdSalle(), salle.getNom(), salle.getAdresse(),
                salle.getVille(), salle.getCapacite());
    }
}

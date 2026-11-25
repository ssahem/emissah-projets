package com.gestionlocative.gestion_locative_api.mapper;

import com.gestionlocative.gestion_locative_api.dto.response.AppartementLibreResponse;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementOccupationResponse;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entité Appartement et son DTO de sortie.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire Appartement ↔ AppartementResponse sans logique métier.
 * DÉPEND DE    : l'entité et le DTO uniquement.
 * NE FAIT PAS  : d'accès base, de HTTP, de règles métier.
 */
@Component
public class AppartementMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit un appartement en réponse HTTP.
     */

    public AppartementResponse toResponse(Appartement appartement) {
        return new AppartementResponse(
                appartement.getIdAppartement(), appartement.getNumero(), appartement.getSurface(),
                appartement.getNbPieces(), appartement.getEtage(), appartement.getIdImmeuble());
    }

    // ── toOccupationResponse() ───────────────────────────────────────────────

    /**
     * Convertit un appartement avec l'état d'occupation calculé par le service
     * (F6).
     */

    public AppartementOccupationResponse toOccupationResponse(
            Appartement appartement, boolean occupe, String statutOccupation) {
        return new AppartementOccupationResponse(
                appartement.getIdAppartement(), appartement.getNumero(), appartement.getSurface(),
                appartement.getEtage(), occupe, statutOccupation);
    }

    // ── toLibreResponse() ────────────────────────────────────────────────────

    /**
     * Convertit un appartement libre avec le dernier loyer fourni par le
     * service (F17).
     */

    public AppartementLibreResponse toLibreResponse(Appartement appartement,
                                                   BigDecimal dernierLoyer) {
        return new AppartementLibreResponse(
                appartement.getIdAppartement(), appartement.getNumero(), appartement.getSurface(),
                dernierLoyer);
    }
}

package com.billetterie.billetterie_api.mapper;

import com.billetterie.billetterie_api.dto.response.EvenementResponse;
import com.billetterie.billetterie_api.dto.response.ProgrammationResponse;
import com.billetterie.billetterie_api.entity.Evenement;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Conversions entre les entités Evenement et leurs DTO.
 *
 * COUCHE       : Mapper
 * RÔLE         : aplatir le spectacle et calculer l'indicateur « à venir » (F7).
 * DÉPEND DE    : Clock pour la comparaison temporelle.
 * NE FAIT PAS  : de règle métier de réservation.
 */
@Component
public class EvenementMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final Clock clock;

    public EvenementMapper(Clock clock) {
        this.clock = clock;
    }

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit un événement en réponse HTTP en aplatissant son spectacle.
     */

    public EvenementResponse toResponse(Evenement evenement) {
        return new EvenementResponse(
                evenement.getIdEvenement(),
                evenement.getSpectacle().getTitre(),
                evenement.getSpectacle().getType(),
                evenement.getSpectacle().getPlacementNumerote(),
                evenement.getDateHeure(),
                evenement.getSalle().getIdSalle(),
                evenement.getSpectacle().getIdSpectacle());
    }

    // ── toProgrammation() ────────────────────────────────────────────────────

    /**
     * Convertit un événement en entrée de programmation, avec l'indicateur «
     * avenir ».
     */

    public ProgrammationResponse toProgrammation(Evenement evenement) {
        boolean avenir = evenement.getDateHeure().isAfter(LocalDateTime.now(clock));
        return new ProgrammationResponse(
                evenement.getIdEvenement(),
                evenement.getSpectacle().getTitre(),
                evenement.getDateHeure(),
                avenir);
    }
}

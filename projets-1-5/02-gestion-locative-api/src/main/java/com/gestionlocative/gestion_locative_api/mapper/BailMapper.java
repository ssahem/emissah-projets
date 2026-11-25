package com.gestionlocative.gestion_locative_api.mapper;

import com.gestionlocative.gestion_locative_api.dto.response.BailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.HistoriqueBailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.LoyerHistoriqueResponse;
import java.util.List;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import org.springframework.stereotype.Component;

/**
 * Conversion entre l'entité Bail et son DTO de sortie.
 *
 * COUCHE       : Mapper
 * RÔLE         : traduire Bail ↔ BailResponse ; le locataire principal est passé
 *                par le service, qui seul connaît le repository de liaison.
 * DÉPEND DE    : l'entité, le DTO et l'entité Locataire.
 * NE FAIT PAS  : d'accès base, de HTTP, de règles métier.
 */
@Component
public class BailMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit un bail en réponse HTTP avec son locataire principal.
     */

    public BailResponse toResponse(Bail bail, Locataire principal) {
        return new BailResponse(
                bail.getIdBail(), bail.getIdAppartement(),
                principal == null ? null : principal.getIdLocataire(),
                principal == null ? null : principal.getNom(),
                principal == null ? null : principal.getPrenom(),
                bail.getDateDebut(), bail.getDateFin(), bail.getLoyerMensuel(),
                bail.getProvisionCharges(), bail.getDepotGarantie(), bail.getStatut(),
                bail.getDateResiliation(), bail.getDateCloture());
    }

    // ── toHistoriqueResponse() ───────────────────────────────────────────────

    /**
     * Convertit un bail pour l'historique d'un appartement (F8).
     */

    public HistoriqueBailResponse toHistoriqueResponse(
            Bail bail, Locataire principal, List<LoyerHistoriqueResponse> loyersPasses,
            boolean enCours) {
        return new HistoriqueBailResponse(
                bail.getIdBail(),
                principal == null ? null : principal.getNom(),
                principal == null ? null : principal.getPrenom(),
                bail.getDateDebut(), bail.getDateFin(), bail.getLoyerMensuel(),
                bail.getStatut(), enCours, loyersPasses);
    }
}

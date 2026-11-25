package com.flottevehicules.flotte_vehicules_api.mapper;

import com.flottevehicules.flotte_vehicules_api.dto.response.SiteResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import org.springframework.stereotype.Component;

/**
 * Conversion Site → SiteResponse.
 *
 * COUCHE       : Mapper (interface HTTP)
 * RÔLE         : isoler les entités JPA du contrat public.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Component
public class SiteMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════


    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit une entité en réponse.
     */

    public SiteResponse toResponse(Site site) {
        return new SiteResponse(site.getIdSite(), site.getNom(), site.getVille(), site.getAdresse());
    }
}

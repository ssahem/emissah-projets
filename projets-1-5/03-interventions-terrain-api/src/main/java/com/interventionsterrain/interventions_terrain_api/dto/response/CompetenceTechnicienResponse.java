package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDate;

/**
 * Compétence obtenue par un technicien, expirée ou non (F7).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : CompetenceTechnicienService puis renvoyé par CompetenceTechnicienController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record CompetenceTechnicienResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idCompetence,
        String libelle,
        LocalDate dateObtention,
        LocalDate dateExpiration,
        boolean expiree) {
}

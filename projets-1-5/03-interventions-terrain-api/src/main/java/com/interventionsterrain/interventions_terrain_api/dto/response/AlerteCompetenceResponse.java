package com.interventionsterrain.interventions_terrain_api.dto.response;

import java.time.LocalDate;

/**
 * Une ligne d'alerte : technicien, compétence et date d'expiration (F16).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR   : AlerteCompetenceService puis renvoyé par AlerteCompetenceController.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
public record AlerteCompetenceResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        Integer idTechnicien,
        String nomTechnicien,
        String libelleCompetence,
        LocalDate dateExpiration) {
}

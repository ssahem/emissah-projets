package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.util.List;

/**
 * Moyennes par matière d'un élève (F17).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Moyennes par matière d'un élève (F17).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RestitutionService.
 * NE FAIT PAS  : de logique métier.
 */
public record MoyennesResponse(
        Integer idEleve, Integer trimestre, List<MoyenneMatiereResponse> matieres) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

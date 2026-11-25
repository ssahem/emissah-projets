package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;

/**
 * Ligne du classement d'un élève (F19).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Ligne du classement d'un élève (F19).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RestitutionService.
 * NE FAIT PAS  : de logique métier. Les élèves sans note n'ont pas de rang (null).
 */
public record LigneClassementResponse(
        Integer idEleve, String nom, String prenom, Integer idInscription,
        BigDecimal moyenneGenerale, Integer rang) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

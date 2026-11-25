package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;

/**
 * Détail du calcul de la moyenne générale, coefficient par coefficient (F18).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Détail du calcul de la moyenne générale, coefficient par coefficient (F18).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : RestitutionService.
 * NE FAIT PAS  : de calcul métier.
 */
public record DetailCoefficientResponse(
        Integer idMatiere, String libelleMatiere, BigDecimal moyenne,
        BigDecimal coefficient, BigDecimal contribution) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

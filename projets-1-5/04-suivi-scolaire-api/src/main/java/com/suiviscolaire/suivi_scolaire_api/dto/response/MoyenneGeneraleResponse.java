package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Moyenne générale d'un élève et détail du calcul (F18).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : RestitutionService.
 * NE FAIT PAS  : de logique métier. Une moyenne absente est null, jamais zéro.
 */
public record MoyenneGeneraleResponse(
        Integer idEleve, Integer trimestre, BigDecimal moyenneGenerale,
        List<DetailCoefficientResponse> detail) {
}

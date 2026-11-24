package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Classement d'une classe (F19).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : RestitutionService.
 * NE FAIT PAS  : de calcul SQL direct.
 */
public record ClassementResponse(
        Integer idClasse, Integer trimestre, BigDecimal moyenneClasse,
        BigDecimal meilleureMoyenne, BigDecimal plusFaibleMoyenne,
        List<LigneClassementResponse> eleves) {
}

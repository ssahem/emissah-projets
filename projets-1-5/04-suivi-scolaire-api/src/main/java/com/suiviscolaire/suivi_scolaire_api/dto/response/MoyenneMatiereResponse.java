package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;

/**
 * Moyenne d'un élève dans une matière (F17).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : RestitutionService.
 * NE FAIT PAS  : de calcul SQL direct (les vues sont lues par VueRepository).
 */
public record MoyenneMatiereResponse(
        Integer idMatiere, String libelleMatiere, BigDecimal coefficient,
        BigDecimal moyenne, int nombreNotes, boolean evaluee) {
}

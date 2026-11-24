package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Notes d'une matière dans une classe, groupées par élève (F8).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : ConsultationService.
 * NE FAIT PAS  : de calcul métier.
 */
public record NotesClasseResponse(
        Integer idClasse, Integer idMatiere, Integer trimestre, BigDecimal moyenneClasse,
        List<NotesEleveResponse> eleves) {
}

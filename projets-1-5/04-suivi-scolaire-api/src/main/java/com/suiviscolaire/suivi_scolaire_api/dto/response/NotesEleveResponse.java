package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Notes d'un élève regroupées dans une matière (F8).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * PRODUIT PAR : ConsultationService.
 * NE FAIT PAS  : de logique métier.
 */
public record NotesEleveResponse(
        Integer idEleve, String nomEleve, String prenomEleve, BigDecimal moyenne,
        List<NoteResponse> notes) {
}

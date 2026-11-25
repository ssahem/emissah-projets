package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Note renvoyée par l'API (F5, F7, F8, F10).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Note renvoyée par l'API (F5, F7, F8, F10).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : NoteService, ConsultationService.
 * NE FAIT PAS  : d'accès base.
 */
public record NoteResponse(
        Integer idNote, Integer idEleve, String nomEleve, Integer idMatiere, String libelleMatiere,
        Integer idClasse, String nomClasse, BigDecimal valeur, Integer trimestre,
        Integer idTypeEval, String libelleTypeEval, BigDecimal poids, LocalDate date) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

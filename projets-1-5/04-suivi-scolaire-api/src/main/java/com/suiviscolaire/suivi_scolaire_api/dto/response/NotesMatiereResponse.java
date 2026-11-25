package com.suiviscolaire.suivi_scolaire_api.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Notes groupées par matière (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Notes groupées par matière (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ConsultationService.
 * NE FAIT PAS  : de calcul métier.
 */
public record NotesMatiereResponse(
        Integer idMatiere, String libelleMatiere, List<NoteResponse> notes) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

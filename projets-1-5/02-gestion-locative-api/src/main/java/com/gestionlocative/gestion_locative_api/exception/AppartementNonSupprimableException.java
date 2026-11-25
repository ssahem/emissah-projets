package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : appartement déjà loué (F3).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : appartement déjà loué (F3).
 * LANCÉE PAR   : AppartementService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : supprimer l'appartement casserait l'historique des baux.
 * NE FAIT PAS  : de logique métier.
 */
public class AppartementNonSupprimableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public AppartementNonSupprimableException(Integer idAppartement) {
        super(HttpStatus.CONFLICT, "Appartement non supprimable", "appartement-non-supprimable",
                "Cet appartement a déjà fait l'objet d'un bail");
        addProperty("idAppartement", idAppartement);
    }
}

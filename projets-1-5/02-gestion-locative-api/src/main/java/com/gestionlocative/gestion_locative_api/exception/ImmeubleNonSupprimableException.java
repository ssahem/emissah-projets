package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : immeuble encore rattaché à des appartements (F2).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : immeuble encore rattaché à des appartements (F2).
 * LANCÉE PAR   : ImmeubleService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : la suppression laisserait des appartements orphelins.
 * NE FAIT PAS  : de logique métier.
 */
public class ImmeubleNonSupprimableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un identifiant donné. */
    public ImmeubleNonSupprimableException(Integer idImmeuble) {
        super(HttpStatus.CONFLICT, "Immeuble non supprimable", "immeuble-non-supprimable",
                "Cet immeuble possède encore des appartements");
        addProperty("idImmeuble", idImmeuble);
    }
}

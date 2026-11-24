package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : tentative de modifier le loyer d'un bail commencé (F5).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : après le début, seule la date de fin reste modifiable ; le loyer
 *                suit la procédure de révision (F15).
 */
public class BailModificationInterditeException extends ApiException {

    /** Construit l'erreur pour un identifiant donné. */
    public BailModificationInterditeException(Integer idBail) {
        super(HttpStatus.CONFLICT, "Modification interdite", "bail-modification-interdite",
                "Le loyer d'un bail commencé ne peut plus être modifié ici ; utiliser la révision");
        addProperty("idBail", idBail);
    }
}

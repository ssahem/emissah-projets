package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : bail déjà commencé, donc non supprimable (F5).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : un bail en cours se résilie (F13), il ne se supprime pas.
 */
public class BailNonSupprimableException extends ApiException {

    /** Construit l'erreur pour un identifiant donné. */
    public BailNonSupprimableException(Integer idBail) {
        super(HttpStatus.CONFLICT, "Bail non supprimable", "bail-non-supprimable",
                "Seul un bail qui n'a pas commencé peut être supprimé ; sinon, le résilier");
        addProperty("idBail", idBail);
    }
}

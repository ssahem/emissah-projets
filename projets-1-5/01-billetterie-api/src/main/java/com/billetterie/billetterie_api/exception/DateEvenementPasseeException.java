package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Date d’événement passée — erreur métier traduite en HTTP 400. */
public class DateEvenementPasseeException extends ApiException {

    // ── DateEvenementPasseeException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public DateEvenementPasseeException() {
        super(HttpStatus.BAD_REQUEST, "Date d’événement passée", "date-evenement-passee", "La date de l’événement doit être postérieure à l’instant présent");
    }
}

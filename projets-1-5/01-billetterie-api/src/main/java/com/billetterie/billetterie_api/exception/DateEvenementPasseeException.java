package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Date d’événement passée — erreur métier traduite en HTTP 400.
 *
 * COUCHE       : Exception
 * RÔLE         : Date d’événement passée — erreur métier traduite en HTTP 400.
 * NE FAIT PAS  : de logique métier.
 */
public class DateEvenementPasseeException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    // ── DateEvenementPasseeException() ───────────────────────────────────────

    /** Construit l'erreur métier. */
    public DateEvenementPasseeException() {
        super(HttpStatus.BAD_REQUEST, "Date d’événement passée", "date-evenement-passee", "La date de l’événement doit être postérieure à l’instant présent");
    }
}

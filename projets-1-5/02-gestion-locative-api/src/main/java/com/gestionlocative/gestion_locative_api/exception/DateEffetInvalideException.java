package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : la date d'effet d'une résiliation est invalide (F13).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : la date d'effet d'une résiliation est invalide (F13).
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : la date doit être comprise dans la période du bail et ne pas
 *                être antérieure à aujourd'hui : c'est une règle de forme.
 * NE FAIT PAS  : de logique métier.
 */
public class DateEffetInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec la date proposée et la période autorisée. */
    public DateEffetInvalideException(Integer idBail, LocalDate dateEffet,
                                      LocalDate dateDebut, LocalDate dateFin) {
        super(HttpStatus.BAD_REQUEST, "Date d'effet invalide", "date-effet-invalide",
                "La date d'effet doit être comprise dans la période du bail et ne pas être "
                        + "antérieure à aujourd'hui");
        addProperty("idBail", idBail);
        addProperty("dateEffet", dateEffet);
        addProperty("dateDebut", dateDebut);
        addProperty("dateFin", dateFin);
    }
}

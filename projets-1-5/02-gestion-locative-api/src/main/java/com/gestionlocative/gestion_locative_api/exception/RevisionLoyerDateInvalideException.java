package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : la date d'effet d'une révision est invalide (F15).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : la date doit être postérieure à la dernière révision et
 *                comprise dans la période du bail.
 */
public class RevisionLoyerDateInvalideException extends ApiException {

    /** Construit l'erreur avec la date proposée et la dernière révision connue. */
    public RevisionLoyerDateInvalideException(Integer idBail, LocalDate dateEffet,
                                              LocalDate derniereRevision,
                                              LocalDate dateDebut, LocalDate dateFin) {
        super(HttpStatus.BAD_REQUEST, "Date de révision invalide", "revision-loyer-date-invalide",
                "La date d'effet doit être postérieure à la dernière révision et comprise "
                        + "dans la période du bail");
        addProperty("idBail", idBail);
        addProperty("dateEffet", dateEffet);
        addProperty("derniereRevision", derniereRevision);
        addProperty("dateDebut", dateDebut);
        addProperty("dateFin", dateFin);
    }
}

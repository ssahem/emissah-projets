package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : la nouvelle échéance d'un renouvellement est invalide (F14).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : la nouvelle échéance d'un renouvellement est invalide (F14).
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : la nouvelle date de fin doit être strictement postérieure à
 *                l'échéance courante pour que le renouvellement ait un effet.
 * NE FAIT PAS  : de logique métier.
 */
public class RenouvellementDateInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec l'ancienne et la nouvelle échéance. */
    public RenouvellementDateInvalideException(Integer idBail, LocalDate dateFinActuelle,
                                               LocalDate nouvelleDateFin) {
        super(HttpStatus.BAD_REQUEST, "Date de renouvellement invalide",
                "renouvellement-date-invalide",
                "La nouvelle date de fin doit être postérieure à l'échéance courante");
        addProperty("idBail", idBail);
        addProperty("dateFinActuelle", dateFinActuelle);
        addProperty("nouvelleDateFin", nouvelleDateFin);
    }
}

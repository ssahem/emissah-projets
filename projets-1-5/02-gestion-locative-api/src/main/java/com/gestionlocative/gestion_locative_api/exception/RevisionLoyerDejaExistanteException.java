package com.gestionlocative.gestion_locative_api.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : une révision existe déjà à cette date (F15).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : une révision existe déjà à cette date (F15).
 * LANCÉE PAR   : BailService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : uq_revision_loyer_bail_date sanctionne un conflit avec une
 *                donnée existante ; le client reçoit un message lisible.
 * NE FAIT PAS  : de logique métier.
 */
public class RevisionLoyerDejaExistanteException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec le bail et la date déjà occupée. */
    public RevisionLoyerDejaExistanteException(Integer idBail, LocalDate dateEffet) {
        super(HttpStatus.CONFLICT, "Révision déjà existante", "revision-loyer-deja-existante",
                "Une révision de loyer existe déjà à cette date pour ce bail");
        addProperty("idBail", idBail);
        addProperty("dateEffet", dateEffet);
    }
}

package com.gestionlocative.gestion_locative_api.exception;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

/**
 * Exception métier : le montant d'une révision de loyer n'est pas positif (F15).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : le montant d'une révision de loyer n'est pas positif (F15).
 * LANCÉE PAR   : BailService, en complément de la validation du DTO.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : ck_revision_loyer_montant existe en V6, mais un 400 explicite
 *                reste préférable à une violation de contrainte SQL.
 * NE FAIT PAS  : de logique métier.
 */
public class RevisionLoyerMontantInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur avec le montant refusé. */
    public RevisionLoyerMontantInvalideException(BigDecimal montant) {
        super(HttpStatus.BAD_REQUEST, "Montant de loyer invalide",
                "revision-loyer-montant-invalide", "Le loyer doit être strictement positif");
        addProperty("montant", montant);
    }
}

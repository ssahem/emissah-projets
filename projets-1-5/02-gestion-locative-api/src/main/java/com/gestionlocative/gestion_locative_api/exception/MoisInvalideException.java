package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : le mois demandé n'est pas au format AAAA-MM (F19).
 *
 * COUCHE       : Exception (erreur métier)
 * LANCÉE PAR   : ImmeubleService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : un mois mal formé ne peut pas être converti en période ; le
 *                refuser ici évite une erreur 500 brute de java.time.
 */
public class MoisInvalideException extends ApiException {

    /** Construit l'erreur avec la valeur reçue. */
    public MoisInvalideException(String mois) {
        super(HttpStatus.BAD_REQUEST, "Mois invalide", "mois-invalide",
                "Le mois doit être au format AAAA-MM (par exemple 2026-11)");
        addProperty("mois", mois);
    }
}

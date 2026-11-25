package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : email déjà utilisé par un autre propriétaire.
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : email déjà utilisé par un autre propriétaire.
 * LANCÉE PAR   : ProprietaireService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : l'unicité est une règle métier, traduite avant la contrainte SQL.
 * NE FAIT PAS  : de logique métier.
 */
public class ProprietaireEmailDejaUtiliseException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un email donné. */
    public ProprietaireEmailDejaUtiliseException(String email) {
        super(HttpStatus.CONFLICT, "Email déjà utilisé", "proprietaire-email-deja-utilise",
                "Cet email est déjà associé à un autre propriétaire");
        addProperty("email", email);
    }
}

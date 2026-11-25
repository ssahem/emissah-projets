package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : email déjà utilisé par un autre locataire (F4).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : email déjà utilisé par un autre locataire (F4).
 * LANCÉE PAR   : LocataireService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : l'unicité doit rester vraie après modification, pas seulement à la création.
 * NE FAIT PAS  : de logique métier.
 */
public class LocataireEmailDejaUtiliseException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un email donné. */
    public LocataireEmailDejaUtiliseException(String email) {
        super(HttpStatus.CONFLICT, "Email déjà utilisé", "locataire-email-deja-utilise",
                "Cet email est déjà associé à un autre locataire");
        addProperty("email", email);
    }
}

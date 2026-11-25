package com.gestionlocative.gestion_locative_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier : numéro déjà utilisé dans l'immeuble (F3).
 *
 * COUCHE       : Exception (erreur métier)
 * RÔLE         : Exception métier : numéro déjà utilisé dans l'immeuble (F3).
 * LANCÉE PAR   : AppartementService.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : uq_appartement_immeuble_numero est une règle métier de création.
 * NE FAIT PAS  : de logique métier.
 */
public class AppartementNumeroDejaUtiliseException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Construit l'erreur pour un numéro et un immeuble. */
    public AppartementNumeroDejaUtiliseException(String numero, Integer idImmeuble) {
        super(HttpStatus.CONFLICT, "Numéro déjà utilisé", "appartement-numero-deja-utilise",
                "Ce numéro est déjà utilisé dans cet immeuble");
        addProperty("numero", numero);
        addProperty("idImmeuble", idImmeuble);
    }
}

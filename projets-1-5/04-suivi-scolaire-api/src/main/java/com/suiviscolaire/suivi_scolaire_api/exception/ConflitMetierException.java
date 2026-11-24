package com.suiviscolaire.suivi_scolaire_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Erreur métier 409 : état ou unicité incompatible.
 *
 * COUCHE       : Exception
 * RÔLE         : signaler un conflit (classe pleine, note figée, doublon…).
 * LANCÉE PAR   : les services métier.
 * INTERCEPTÉE  : GlobalExceptionHandler → 409 Problem Details.
 * NE FAIT PAS  : de logique métier.
 */
public class ConflitMetierException extends ApiException {

    public ConflitMetierException(String type, String detail) {
        super(HttpStatus.CONFLICT, "Conflit métier", type, detail);
    }

    public ConflitMetierException(String type, String detail, Map<String, Object> properties) {
        this(type, detail);
        properties.forEach(this::addProperty);
    }
}

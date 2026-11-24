package com.flottevehicules.flotte_vehicules_api.exception;

import org.springframework.http.HttpStatus;

/**
 * État incompatible avec l'opération demandée (doublon, chevauchement, ressource utilisée).
 *
 * COUCHE       : Exception
 * LANCÉE PAR   : les services de la flotte.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : exposer un motif métier précis, sans jamais révéler de SQL.
 */
public class ConflitMetierException extends ApiException {

    public ConflitMetierException(String type, String detail) {
        super(HttpStatus.CONFLICT, "Conflit métier", type, detail);
    }

    /** Ajoute une donnée complémentaire au corps de l'erreur. */
    public ConflitMetierException avec(String cle, Object valeur) {
        addProperty(cle, valeur);
        return this;
    }
}

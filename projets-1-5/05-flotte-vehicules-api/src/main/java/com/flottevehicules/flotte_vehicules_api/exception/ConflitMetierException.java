package com.flottevehicules.flotte_vehicules_api.exception;

import org.springframework.http.HttpStatus;

/**
 * État incompatible avec l'opération demandée (doublon, chevauchement, ressource utilisée).
 *
 * COUCHE       : Exception
 * RÔLE         : État incompatible avec l'opération demandée (doublon, chevauchement, ressource utilisée).
 * LANCÉE PAR   : les services de la flotte.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 409.
 * POURQUOI     : exposer un motif métier précis, sans jamais révéler de SQL.
 * NE FAIT PAS  : de logique métier.
 */
public class ConflitMetierException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    public ConflitMetierException(String type, String detail) {
        super(HttpStatus.CONFLICT, "Conflit métier", type, detail);
    }


    // ── avec() ───────────────────────────────────────────────────────────────

    /**
     * Ajoute une donnée complémentaire au corps de l'erreur.
     */

    public ConflitMetierException avec(String cle, Object valeur) {
        addProperty(cle, valeur);
        return this;
    }
}

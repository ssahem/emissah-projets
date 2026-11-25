package com.flottevehicules.flotte_vehicules_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Donnée métier invalide (date passée ou future, kilométrage en recul, référence inconnue).
 *
 * COUCHE       : Exception
 * RÔLE         : Donnée métier invalide (date passée ou future, kilométrage en recul, référence inconnue).
 * LANCÉE PAR   : les services de la flotte.
 * INTERCEPTÉE  : par GlobalExceptionHandler → HTTP 400.
 * POURQUOI     : distinguer une règle métier d'une simple erreur de format JSON.
 * NE FAIT PAS  : de logique métier.
 */
public class DonneeInvalideException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    public DonneeInvalideException(String type, String detail) {
        super(HttpStatus.BAD_REQUEST, "Donnée invalide", type, detail);
    }

    /** Ajoute une donnée complémentaire au corps de l'erreur. */

    // ── avec() ───────────────────────────────────────────────────────────────

    /**
     * Exécute avec().
     */

    public DonneeInvalideException avec(String cle, Object valeur) {
        addProperty(cle, valeur);
        return this;
    }
}

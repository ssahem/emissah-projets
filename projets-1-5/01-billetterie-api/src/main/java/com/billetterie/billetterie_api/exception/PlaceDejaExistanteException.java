package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Place déjà existante — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Place déjà existante — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class PlaceDejaExistanteException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String rang;

    // ── PlaceDejaExistanteException() ────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlaceDejaExistanteException(String rang) {
        super(HttpStatus.CONFLICT, "Place déjà existante", "place-deja-existante", "Cette position existe déjà dans la salle");
        this.rang = rang;
        addProperty("rang", rang);
    }

    // ── getRang() ────────────────────────────────────────────────────────────

    /**
     * Retourne rang.
     */

    public String getRang() { return rang; }
}

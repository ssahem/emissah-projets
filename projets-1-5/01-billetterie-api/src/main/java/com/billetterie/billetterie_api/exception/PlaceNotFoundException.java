package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Place introuvable — erreur métier traduite en HTTP not.
 *
 * COUCHE       : Exception
 * RÔLE         : Place introuvable — erreur métier traduite en HTTP not.
 * NE FAIT PAS  : de logique métier.
 */
public class PlaceNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String rang;

    // ── PlaceNotFoundException() ─────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlaceNotFoundException(String rang) {
        super(HttpStatus.NOT_FOUND, "Place introuvable", "place-not-found", "Aucune place ne correspond à cette position");
        this.rang = rang;
        addProperty("rang", rang);
    }

    // ── getRang() ────────────────────────────────────────────────────────────

    /** Retourne rang. */
    public String getRang() { return rang; }
}

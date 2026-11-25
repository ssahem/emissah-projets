package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Place déjà réservée — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Place déjà réservée — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class PlaceDejaReserveeException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer numero;

    // ── PlaceDejaReserveeException() ─────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlaceDejaReserveeException(Integer numero) {
        super(HttpStatus.CONFLICT, "Place déjà réservée", "place-deja-reservee", "Cette place est déjà réservée pour cet événement");
        this.numero = numero;
        addProperty("numero", numero);
    }

    // ── getNumero() ──────────────────────────────────────────────────────────

    /**
     * Retourne numero.
     */

    public Integer getNumero() { return numero; }
}

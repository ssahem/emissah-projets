package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Réservation expirée — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Réservation expirée — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class ReservationExpireeException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idReservation;

    // ── ReservationExpireeException() ────────────────────────────────────────

    /** Construit l'erreur métier. */
    public ReservationExpireeException(Integer idReservation) {
        super(HttpStatus.CONFLICT, "Réservation expirée", "reservation-expiree", "Le délai de quinze minutes est dépassé");
        this.idReservation = idReservation;
        addProperty("idReservation", idReservation);
    }

    // ── getIdReservation() ───────────────────────────────────────────────────

    /**
     * Retourne idReservation.
     */

    public Integer getIdReservation() { return idReservation; }
}

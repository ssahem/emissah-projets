package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Réservation non supprimable — erreur métier traduite en HTTP conflict. */
public class ReservationNonSupprimableException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idReservation;

    // ── ReservationNonSupprimableException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public ReservationNonSupprimableException(Integer idReservation) {
        super(HttpStatus.CONFLICT, "Réservation non supprimable", "reservation-non-supprimable", "Seules les réservations provisoires ou annulées peuvent être supprimées");
        this.idReservation = idReservation;
        addProperty("idReservation", idReservation);
    }

    // ── getIdReservation() ───────────────────────────────────────

    /** Retourne idReservation. */
    public Integer getIdReservation() { return idReservation; }
}

package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Réservation introuvable — erreur métier traduite en HTTP not. */
public class ReservationNotFoundException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idReservation;

    // ── ReservationNotFoundException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public ReservationNotFoundException(Integer idReservation) {
        super(HttpStatus.NOT_FOUND, "Réservation introuvable", "reservation-not-found", "Aucune réservation ne correspond à cet identifiant");
        this.idReservation = idReservation;
        addProperty("idReservation", idReservation);
    }

    // ── getIdReservation() ───────────────────────────────────────

    /** Retourne idReservation. */
    public Integer getIdReservation() { return idReservation; }
}

package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Annulation impossible — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Annulation impossible — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class ReservationAnnulationImpossibleException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idReservation;

    // ── ReservationAnnulationImpossibleException() ───────────────────────────

    /** Construit l'erreur métier. */
    public ReservationAnnulationImpossibleException(Integer idReservation) {
        super(HttpStatus.CONFLICT, "Annulation impossible", "reservation-annulation-impossible",
                "Cette réservation ne peut pas être annulée");
        this.idReservation = idReservation;
        addProperty("idReservation", idReservation);
    }

    // ── getIdReservation() ───────────────────────────────────────────────────

    /** Retourne idReservation. */
    public Integer getIdReservation() { return idReservation; }
}

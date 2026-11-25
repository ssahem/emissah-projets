package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Statut incompatible — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Statut incompatible — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class StatutReservationIncompatibleException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idReservation;

    // ── StatutReservationIncompatibleException() ─────────────────────────────

    /** Construit l'erreur métier. */
    public StatutReservationIncompatibleException(Integer idReservation) {
        super(HttpStatus.CONFLICT, "Statut incompatible", "statut-reservation-incompatible", "Le statut de la réservation ne permet pas cette opération");
        this.idReservation = idReservation;
        addProperty("idReservation", idReservation);
    }

    // ── getIdReservation() ───────────────────────────────────────────────────

    /**
     * Retourne idReservation.
     */

    public Integer getIdReservation() { return idReservation; }
}

package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Position de place figée — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Position de place figée — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class PlacePositionFigeeException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idPlace;

    // ── PlacePositionFigeeException() ────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlacePositionFigeeException(Integer idPlace) {
        super(HttpStatus.CONFLICT, "Position figée", "place-position-figee",
                "Le rang, le numéro et la salle d'une place réservée ne peuvent pas changer");
        this.idPlace = idPlace;
        addProperty("idPlace", idPlace);
    }

    // ── getIdPlace() ─────────────────────────────────────────────────────────

    /** Retourne idPlace. */
    public Integer getIdPlace() { return idPlace; }
}

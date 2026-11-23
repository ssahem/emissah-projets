package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Salle sans place — erreur métier traduite en HTTP conflict. */
public class SalleSansPlaceException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idSalle;

    // ── SalleSansPlaceException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public SalleSansPlaceException(Integer idSalle) {
        super(HttpStatus.CONFLICT, "Salle sans place", "salle-sans-place", "La salle doit contenir au moins une place");
        this.idSalle = idSalle;
        addProperty("idSalle", idSalle);
    }

    // ── getIdSalle() ───────────────────────────────────────

    /** Retourne idSalle. */
    public Integer getIdSalle() { return idSalle; }
}

package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Capacité insuffisante — erreur métier traduite en HTTP conflict. */
public class CapaciteInsuffisanteException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idSalle;

    // ── CapaciteInsuffisanteException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public CapaciteInsuffisanteException(Integer idSalle) {
        super(HttpStatus.CONFLICT, "Capacité insuffisante", "capacite-insuffisante", "La capacité ne peut pas être inférieure au nombre de places existantes");
        this.idSalle = idSalle;
        addProperty("idSalle", idSalle);
    }

    // ── getIdSalle() ───────────────────────────────────────

    /** Retourne idSalle. */
    public Integer getIdSalle() { return idSalle; }
}

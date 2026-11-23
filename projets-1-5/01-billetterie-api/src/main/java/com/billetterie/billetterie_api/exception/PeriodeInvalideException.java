package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Période invalide — erreur métier traduite en HTTP bad. */
public class PeriodeInvalideException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String du;

    // ── PeriodeInvalideException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PeriodeInvalideException(String du) {
        super(HttpStatus.BAD_REQUEST, "Période invalide", "periode-invalide", "La date de début doit être antérieure à la date de fin");
        this.du = du;
        addProperty("du", du);
    }

    // ── getDu() ───────────────────────────────────────

    /** Retourne du. */
    public String getDu() { return du; }
}

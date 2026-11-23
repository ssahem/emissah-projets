package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Salle non supprimable — erreur métier traduite en HTTP conflict. */
public class SalleNonSupprimableException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idSalle;

    // ── SalleNonSupprimableException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public SalleNonSupprimableException(Integer idSalle) {
        super(HttpStatus.CONFLICT, "Salle non supprimable", "salle-non-supprimable", "Cette salle possède encore des places ou des événements");
        this.idSalle = idSalle;
        addProperty("idSalle", idSalle);
    }

    // ── getIdSalle() ───────────────────────────────────────

    /** Retourne idSalle. */
    public Integer getIdSalle() { return idSalle; }
}

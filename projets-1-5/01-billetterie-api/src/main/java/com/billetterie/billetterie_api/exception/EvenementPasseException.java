package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Événement passé — erreur métier traduite en HTTP bad. */
public class EvenementPasseException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idEvenement;

    // ── EvenementPasseException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public EvenementPasseException(Integer idEvenement) {
        super(HttpStatus.BAD_REQUEST, "Événement passé", "evenement-passe", "Cet événement a déjà eu lieu");
        this.idEvenement = idEvenement;
        addProperty("idEvenement", idEvenement);
    }

    // ── getIdEvenement() ───────────────────────────────────────

    /** Retourne idEvenement. */
    public Integer getIdEvenement() { return idEvenement; }
}

package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Salle figée — erreur métier traduite en HTTP conflict. */
public class SalleEvenementFigeeException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idEvenement;

    // ── SalleEvenementFigeeException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public SalleEvenementFigeeException(Integer idEvenement) {
        super(HttpStatus.CONFLICT, "Salle figée", "salle-evenement-figee", "La salle ne peut plus changer car une réservation existe");
        this.idEvenement = idEvenement;
        addProperty("idEvenement", idEvenement);
    }

    // ── getIdEvenement() ───────────────────────────────────────

    /** Retourne idEvenement. */
    public Integer getIdEvenement() { return idEvenement; }
}

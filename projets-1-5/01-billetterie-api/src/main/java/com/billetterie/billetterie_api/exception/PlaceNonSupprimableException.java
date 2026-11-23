package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Place non supprimable — erreur métier traduite en HTTP conflict. */
public class PlaceNonSupprimableException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idPlace;

    // ── PlaceNonSupprimableException() ────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlaceNonSupprimableException(Integer idPlace) {
        super(HttpStatus.CONFLICT, "Place non supprimable", "place-non-supprimable",
                "Cette place fait l'objet d'une réservation");
        this.idPlace = idPlace;
        addProperty("idPlace", idPlace);
    }

    // ── getIdPlace() ───────────────────────────────────────

    /** Retourne idPlace. */
    public Integer getIdPlace() { return idPlace; }
}

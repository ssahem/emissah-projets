package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Place hors salle — erreur métier traduite en HTTP bad.
 *
 * COUCHE       : Exception
 * RÔLE         : Place hors salle — erreur métier traduite en HTTP bad.
 * NE FAIT PAS  : de logique métier.
 */
public class PlaceHorsSalleException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer numero;

    // ── PlaceHorsSalleException() ────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public PlaceHorsSalleException(Integer numero) {
        super(HttpStatus.BAD_REQUEST, "Place hors salle", "place-hors-salle", "La place n’appartient pas à la salle de cet événement");
        this.numero = numero;
        addProperty("numero", numero);
    }

    // ── getNumero() ──────────────────────────────────────────────────────────

    /** Retourne numero. */
    public Integer getNumero() { return numero; }
}

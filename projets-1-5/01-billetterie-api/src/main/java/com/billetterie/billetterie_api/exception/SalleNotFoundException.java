package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Salle introuvable — erreur métier traduite en HTTP not.
 *
 * COUCHE       : Exception
 * RÔLE         : Salle introuvable — erreur métier traduite en HTTP not.
 * NE FAIT PAS  : de logique métier.
 */
public class SalleNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idSalle;

    // ── SalleNotFoundException() ─────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public SalleNotFoundException(Integer idSalle) {
        super(HttpStatus.NOT_FOUND, "Salle introuvable", "salle-not-found", "Aucune salle ne correspond à cet identifiant");
        this.idSalle = idSalle;
        addProperty("idSalle", idSalle);
    }

    // ── getIdSalle() ─────────────────────────────────────────────────────────

    /**
     * Retourne idSalle.
     */

    public Integer getIdSalle() { return idSalle; }
}

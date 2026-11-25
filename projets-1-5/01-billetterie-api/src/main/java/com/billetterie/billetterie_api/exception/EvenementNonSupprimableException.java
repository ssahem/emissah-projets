package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Événement non supprimable — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Événement non supprimable — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class EvenementNonSupprimableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idEvenement;

    // ── EvenementNonSupprimableException() ───────────────────────────────────

    /** Construit l'erreur métier. */
    public EvenementNonSupprimableException(Integer idEvenement) {
        super(HttpStatus.CONFLICT, "Événement non supprimable", "evenement-non-supprimable", "Cet événement possède encore des réservations");
        this.idEvenement = idEvenement;
        addProperty("idEvenement", idEvenement);
    }

    // ── getIdEvenement() ─────────────────────────────────────────────────────

    /**
     * Retourne idEvenement.
     */

    public Integer getIdEvenement() { return idEvenement; }
}

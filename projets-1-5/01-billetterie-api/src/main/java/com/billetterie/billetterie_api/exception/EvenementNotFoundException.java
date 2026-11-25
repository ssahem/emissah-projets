package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Événement introuvable — erreur métier traduite en HTTP not.
 *
 * COUCHE       : Exception
 * RÔLE         : Événement introuvable — erreur métier traduite en HTTP not.
 * NE FAIT PAS  : de logique métier.
 */
public class EvenementNotFoundException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final Integer idEvenement;

    // ── EvenementNotFoundException() ─────────────────────────────────────────

    /** Construit l'erreur métier. */
    public EvenementNotFoundException(Integer idEvenement) {
        super(HttpStatus.NOT_FOUND, "Événement introuvable", "evenement-not-found", "Aucun événement ne correspond à cet identifiant");
        this.idEvenement = idEvenement;
        addProperty("idEvenement", idEvenement);
    }

    // ── getIdEvenement() ─────────────────────────────────────────────────────

    /** Retourne idEvenement. */
    public Integer getIdEvenement() { return idEvenement; }
}

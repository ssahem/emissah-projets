package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Salle déjà existante — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Salle déjà existante — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class SalleDejaExistanteException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String nom;

    // ── SalleDejaExistanteException() ────────────────────────────────────────

    /** Construit l'erreur métier. */
    public SalleDejaExistanteException(String nom) {
        super(HttpStatus.CONFLICT, "Salle déjà existante", "salle-deja-existante", "Une salle porte déjà ce nom dans cette ville");
        this.nom = nom;
        addProperty("nom", nom);
    }

    // ── getNom() ─────────────────────────────────────────────────────────────

    /**
     * Retourne nom.
     */

    public String getNom() { return nom; }
}

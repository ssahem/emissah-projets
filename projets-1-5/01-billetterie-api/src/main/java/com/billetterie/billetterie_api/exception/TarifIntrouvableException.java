package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/**
 * Tarif introuvable — erreur métier traduite en HTTP conflict.
 *
 * COUCHE       : Exception
 * RÔLE         : Tarif introuvable — erreur métier traduite en HTTP conflict.
 * NE FAIT PAS  : de logique métier.
 */
public class TarifIntrouvableException extends ApiException {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String categorie;

    // ── TarifIntrouvableException() ──────────────────────────────────────────

    /** Construit l'erreur métier. */
    public TarifIntrouvableException(String categorie) {
        super(HttpStatus.CONFLICT, "Tarif introuvable", "tarif-introuvable", "Aucun tarif n’est défini pour cette catégorie");
        this.categorie = categorie;
        addProperty("categorie", categorie);
    }

    // ── getCategorie() ───────────────────────────────────────────────────────

    /**
     * Retourne categorie.
     */

    public String getCategorie() { return categorie; }
}

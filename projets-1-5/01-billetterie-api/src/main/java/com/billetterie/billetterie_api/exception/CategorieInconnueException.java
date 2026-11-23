package com.billetterie.billetterie_api.exception;

import org.springframework.http.HttpStatus;

/** Catégorie inconnue — erreur métier traduite en HTTP bad. */
public class CategorieInconnueException extends ApiException {

    /** Valeur fautive, exposée dans le Problem Detail. */
    private final String categorie;

    // ── CategorieInconnueException() ─────────────────────────────────────────────────────────────

    /** Construit l'erreur métier. */
    public CategorieInconnueException(String categorie) {
        super(HttpStatus.BAD_REQUEST, "Catégorie inconnue", "categorie-inconnue", "Cette catégorie n’existe pas");
        this.categorie = categorie;
        addProperty("categorie", categorie);
    }

    // ── getCategorie() ───────────────────────────────────────

    /** Retourne categorie. */
    public String getCategorie() { return categorie; }
}

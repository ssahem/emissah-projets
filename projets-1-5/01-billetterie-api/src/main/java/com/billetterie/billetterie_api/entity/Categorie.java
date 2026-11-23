package com.billetterie.billetterie_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Catégorie de place (ORCHESTRE, BALCON, LOGE).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Categorie sous forme d’objet Java.
 * IDENTITÉ     : libelle.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "categorie")
public class Categorie {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Integer idCategorie;
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Categorie() ──────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Categorie() {}

    // ── Categorie(String libelle) ────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Categorie(String libelle) { this.libelle = Objects.requireNonNull(libelle, "libelle"); }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdCategorie() ─────────────────────────────────────────────────

    /** Retourne idCategorie. */
    public Integer getIdCategorie() { return idCategorie; }

    // ── getLibelle() ─────────────────────────────────────────────────────

    /** Retourne libelle. */
    public String getLibelle() { return libelle; }
}

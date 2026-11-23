package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Catégorie de place (ORCHESTRE, BALCON, LOGE).
 * COUCHE : Entité · IDENTITÉ : libelle · NE FAIT PAS : d'exposition d'enum Java. */
@Entity
@Table(name = "categorie")
public class Categorie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Integer idCategorie;
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle;
    protected Categorie() {}
    public Categorie(String libelle) { this.libelle = Objects.requireNonNull(libelle, "libelle"); }
    public Integer getIdCategorie() { return idCategorie; }
    public String getLibelle() { return libelle; }
}

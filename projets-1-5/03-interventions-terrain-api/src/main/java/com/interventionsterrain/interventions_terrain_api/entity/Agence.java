package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** Agence d'affectation des équipes (F1). */
@Entity
@Table(name = "agence")
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agence")
    private Integer idAgence;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @Column(name = "responsable", length = 100)
    private String responsable;

    protected Agence() {
    }

    public Agence(String nom, String adresse, String ville, String responsable) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.responsable = responsable;
    }

    public Integer getIdAgence() { return idAgence; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public String getVille() { return ville; }
    public String getResponsable() { return responsable; }

    public void modifier(String nom, String responsable, String ville) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.responsable = responsable;
        this.ville = Objects.requireNonNull(ville, "ville");
    }
}

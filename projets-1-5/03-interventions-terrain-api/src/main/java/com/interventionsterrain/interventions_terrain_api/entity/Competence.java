package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/** Compétence du référentiel (F3). */
@Entity
@Table(name = "competence")
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competence")
    private Integer idCompetence;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "description")
    private String description;

    protected Competence() {
    }

    public Competence(String libelle, String description) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.description = description;
    }

    public Integer getIdCompetence() { return idCompetence; }
    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }

    public void modifier(String libelle, String description) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.description = description;
    }
}

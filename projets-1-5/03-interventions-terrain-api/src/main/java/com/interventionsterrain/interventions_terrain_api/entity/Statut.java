package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ligne du référentiel statut (V7), convertie en enum applicatif via son libellé (E-14). */
@Entity
@Table(name = "statut")
public class Statut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Integer idStatut;

    @Column(name = "libelle", nullable = false, length = 30)
    private String libelle;

    protected Statut() {
    }

    public Integer getIdStatut() { return idStatut; }
    public String getLibelle() { return libelle; }
}

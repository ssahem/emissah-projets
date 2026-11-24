package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/** Adresse du lieu d'une intervention (E-21), distincte de l'adresse texte du client. */
@Entity
@Table(name = "adresse")
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresse")
    private Integer idAdresse;

    @Column(name = "voie", nullable = false, length = 255)
    private String voie;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "commune", nullable = false)
    private String commune;

    protected Adresse() {
    }

    public Adresse(String voie, String commune) {
        this.voie = Objects.requireNonNull(voie, "voie");
        this.commune = Objects.requireNonNull(commune, "commune");
    }

    public Integer getIdAdresse() { return idAdresse; }
    public String getVoie() { return voie; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public String getCommune() { return commune; }
}

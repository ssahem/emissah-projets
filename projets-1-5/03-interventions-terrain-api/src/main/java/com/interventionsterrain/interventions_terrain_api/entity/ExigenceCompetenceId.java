package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Clé primaire composite de exigence_competence. */
@Embeddable
public class ExigenceCompetenceId implements Serializable {

    @Column(name = "id_type")
    private Integer idType;

    @Column(name = "id_competence")
    private Integer idCompetence;

    protected ExigenceCompetenceId() {
    }

    public ExigenceCompetenceId(Integer idType, Integer idCompetence) {
        this.idType = Objects.requireNonNull(idType, "idType");
        this.idCompetence = Objects.requireNonNull(idCompetence, "idCompetence");
    }

    public Integer getIdType() { return idType; }
    public Integer getIdCompetence() { return idCompetence; }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof ExigenceCompetenceId that)) { return false; }
        return Objects.equals(idType, that.idType) && Objects.equals(idCompetence, that.idCompetence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idType, idCompetence);
    }
}

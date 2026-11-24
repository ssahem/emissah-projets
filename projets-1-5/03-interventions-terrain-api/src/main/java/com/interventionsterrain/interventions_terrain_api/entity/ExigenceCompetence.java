package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Compétence exigée par un type d'intervention (F3, F13). Clé composée, pas de clé technique. */
@Entity
@Table(name = "exigence_competence")
public class ExigenceCompetence {

    @EmbeddedId
    private ExigenceCompetenceId id;

    protected ExigenceCompetence() {
    }

    public ExigenceCompetence(Integer idType, Integer idCompetence) {
        this.id = new ExigenceCompetenceId(idType, idCompetence);
    }

    public ExigenceCompetenceId getId() { return id; }
}

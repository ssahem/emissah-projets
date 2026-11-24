package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clé primaire composite de exigence_competence.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : la clé primaire.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Embeddable
public class ExigenceCompetenceId implements Serializable {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Column(name = "id_type")
    private Integer idType;

    @Column(name = "id_competence")
    private Integer idCompetence;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected ExigenceCompetenceId() {
    }

    public ExigenceCompetenceId(Integer idType, Integer idCompetence) {
        this.idType = Objects.requireNonNull(idType, "idType");
        this.idCompetence = Objects.requireNonNull(idCompetence, "idCompetence");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdType() { return idType; }
    public Integer getIdCompetence() { return idCompetence; }

    @Override
    // ═════════════════════════════════════════════════════════════════════════
    //  IDENTITÉ ET AFFICHAGE
    // ═════════════════════════════════════════════════════════════════════════

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

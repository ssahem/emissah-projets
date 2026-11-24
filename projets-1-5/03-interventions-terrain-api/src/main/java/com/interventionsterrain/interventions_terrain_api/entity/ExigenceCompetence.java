package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Compétence exigée par un type d'intervention (F3, F13). Clé composée, pas de clé technique.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : la clé primaire.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "exigence_competence")
public class ExigenceCompetence {

    @EmbeddedId
    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private ExigenceCompetenceId id;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected ExigenceCompetence() {
    }

    public ExigenceCompetence(Integer idType, Integer idCompetence) {
        this.id = new ExigenceCompetenceId(idType, idCompetence);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public ExigenceCompetenceId getId() { return id; }
}

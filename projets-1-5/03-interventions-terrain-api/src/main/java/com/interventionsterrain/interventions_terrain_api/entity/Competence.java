package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Compétence du référentiel (F3).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idCompetence.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "competence")
public class Competence {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competence")
    private Integer idCompetence;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "description")
    private String description;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Competence() {
    }

    public Competence(String libelle, String description) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.description = description;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdCompetence() { return idCompetence; }
    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(String libelle, String description) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.description = description;
    }
}

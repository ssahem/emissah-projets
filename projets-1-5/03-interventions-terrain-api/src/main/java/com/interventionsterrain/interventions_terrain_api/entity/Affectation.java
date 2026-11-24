package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Affectation d'un technicien à une intervention ; un seul responsable par intervention (A-02).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idAffectation.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "affectation")
public class Affectation {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Integer idAffectation;

    @Column(name = "est_responsable", nullable = false)
    private boolean estResponsable;

    @Column(name = "id_intervention", nullable = false)
    private Integer idIntervention;

    @Column(name = "id_technicien", nullable = false)
    private Integer idTechnicien;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Affectation() {
    }

    public Affectation(Integer idIntervention, Integer idTechnicien, boolean estResponsable) {
        this.idIntervention = Objects.requireNonNull(idIntervention, "idIntervention");
        this.idTechnicien = Objects.requireNonNull(idTechnicien, "idTechnicien");
        this.estResponsable = estResponsable;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdAffectation() { return idAffectation; }
    public boolean isEstResponsable() { return estResponsable; }
    public Integer getIdIntervention() { return idIntervention; }
    public Integer getIdTechnicien() { return idTechnicien; }

    public void setEstResponsable(boolean estResponsable) {
        this.estResponsable = estResponsable;
    }
}

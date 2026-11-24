package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Équipe rattachée à une agence : seul lien entre technicien et agence (A-01).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idEquipe.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "equipe")
public class Equipe {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipe")
    private Integer idEquipe;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "id_agence", nullable = false)
    private Integer idAgence;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Equipe() {
    }

    public Equipe(String nom, Integer idAgence) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.idAgence = Objects.requireNonNull(idAgence, "idAgence");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdEquipe() { return idEquipe; }
    public String getNom() { return nom; }
    public Integer getIdAgence() { return idAgence; }
}

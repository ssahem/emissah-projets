package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historique du statut d'une affectation (A-03) : le dernier ligne fait foi.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idHistorique.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "id_statut", nullable = false)
    private Integer idStatut;

    @Column(name = "id_affectation", nullable = false)
    private Integer idAffectation;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected HistoriqueStatut() {
    }

    public HistoriqueStatut(LocalDateTime dateHeure, Integer idStatut, Integer idAffectation) {
        this.dateHeure = Objects.requireNonNull(dateHeure, "dateHeure");
        this.idStatut = Objects.requireNonNull(idStatut, "idStatut");
        this.idAffectation = Objects.requireNonNull(idAffectation, "idAffectation");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdHistorique() { return idHistorique; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public Integer getIdStatut() { return idStatut; }
    public Integer getIdAffectation() { return idAffectation; }
}

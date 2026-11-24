package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Trajet réalisé dans le cadre d'une affectation.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code trajet}, source du compteur courant.
 * IDENTITÉ     : l'identifiant technique.
 * UTILISÉ PAR  : TrajetRepository, vues SQL.
 * NE FAIT PAS  : de contrôle de cohérence kilométrique (les vues le signalent).
 */
@Entity
@Table(name = "trajet")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trajet")
    private Integer idTrajet;

    @Column(name = "date_trajet", nullable = false)
    private LocalDate dateTrajet;

    @Column(name = "km_depart", nullable = false)
    private Integer kmDepart;

    @Column(name = "km_arrivee", nullable = false)
    private Integer kmArrivee;

    @Column(name = "motif")
    private String motif;

    @Column(name = "id_affectation", nullable = false)
    private Integer idAffectation;

    /** Constructeur vide exigé par JPA. */
    protected Trajet() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdTrajet() { return idTrajet; }

    /** Retourne la date. */
    public LocalDate getDateTrajet() { return dateTrajet; }

    /** Retourne le kilométrage au départ. */
    public Integer getKmDepart() { return kmDepart; }

    /** Retourne le kilométrage à l'arrivée. */
    public Integer getKmArrivee() { return kmArrivee; }

    /** Retourne le motif. */
    public String getMotif() { return motif; }

    /** Retourne l'affectation. */
    public Integer getIdAffectation() { return idAffectation; }
}

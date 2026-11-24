package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catégorie de dépense, avec ou sans conducteur.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code type_depense}.
 * IDENTITÉ     : le libellé, unique dans le schéma.
 * UTILISÉ PAR  : DepenseRepository, vues SQL.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "type_depense")
public class TypeDepense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_depense")
    private Integer idTypeDepense;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "avec_conducteur", nullable = false)
    private Boolean avecConducteur;

    /** Constructeur vide exigé par JPA. */
    protected TypeDepense() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdTypeDepense() { return idTypeDepense; }

    /** Retourne le libellé. */
    public String getLibelle() { return libelle; }

    /** Indique si la dépense implique un conducteur. */
    public Boolean getAvecConducteur() { return avecConducteur; }
}

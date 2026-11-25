package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Type d'entretien et ses periodicités (F15).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code type_entretien}.
 * IDENTITÉ     : le libellé, unique dans le schéma.
 * UTILISÉ PAR  : EntretienRepository, VueRepository, EntretienService.
 * NE FAIT PAS  : de calcul d'échéance.
 */
@Entity
@Table(name = "type_entretien")
public class TypeEntretien {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_entretien")
    private Integer idTypeEntretien;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "periodicite_km", nullable = false)
    private Integer periodiciteKm;

    @Column(name = "periodicite_mois", nullable = false)
    private Integer periodiciteMois;

    @Column(name = "categorie")
    private String categorie;

    /** Constructeur vide exigé par JPA. */
    public TypeEntretien() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdTypeEntretien() { return idTypeEntretien; }

    /** Retourne le libellé. */
    public String getLibelle() { return libelle; }

    /** Retourne la periodicité kilométrique. */
    public Integer getPeriodiciteKm() { return periodiciteKm; }

    /** Retourne la periodicité en mois. */
    public Integer getPeriodiciteMois() { return periodiciteMois; }

    /** Retourne la catégorie (PERIODIQUE ou CORRECTIF). */
    public String getCategorie() { return categorie; }
}

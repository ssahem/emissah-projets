package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Prestataire d'entretien.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code prestataire}.
 * IDENTITÉ     : le nom.
 * UTILISÉ PAR  : PrestataireRepository, EntretienService.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "prestataire")
public class Prestataire {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestataire")
    private Integer idPrestataire;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "telephone")
    private String telephone;

    /** Constructeur vide exigé par JPA. */
    public Prestataire() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdPrestataire() { return idPrestataire; }

    /** Retourne le nom. */
    public String getNom() { return nom; }

    /** Retourne l'adresse. */
    public String getAdresse() { return adresse; }

    /** Retourne le téléphone. */
    public String getTelephone() { return telephone; }
}

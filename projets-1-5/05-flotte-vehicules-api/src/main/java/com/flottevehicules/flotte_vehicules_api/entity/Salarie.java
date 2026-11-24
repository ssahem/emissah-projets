package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Fiche d'un salarié utilisateur d'un véhicule (F3).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code salarie}.
 * IDENTITÉ     : le matricule, unique depuis V2.
 * UTILISÉ PAR  : SalarieRepository, SalarieService.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP.
 */
@Entity
@Table(name = "salarie")
public class Salarie {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salarie")
    private Integer idSalarie;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "matricule", unique = true)
    private String matricule;

    @Column(name = "service")
    private String service;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Salarie() {
    }

    /** Crée un salarié avec ses champs obligatoires. */
    public Salarie(String nom, String prenom, String email, String matricule, String service) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.matricule = Objects.requireNonNull(matricule, "matricule");
        this.service = Objects.requireNonNull(service, "service");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdSalarie() { return idSalarie; }

    /** Retourne le nom. */
    public String getNom() { return nom; }

    /** Retourne le prénom. */
    public String getPrenom() { return prenom; }

    /** Retourne l'adresse électronique. */
    public String getEmail() { return email; }

    /** Retourne le matricule. */
    public String getMatricule() { return matricule; }

    /** Retourne le service. */
    public String getService() { return service; }

    // ═════════════════════════════════════════════════════════════════════════
    //  COMPORTEMENT
    // ═════════════════════════════════════════════════════════════════════════

    /** Modifie les champs autorisés ; le matricule est figé (F3). */
    public void modifier(String nom, String prenom, String email, String service) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.service = Objects.requireNonNull(service, "service");
    }
}

package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Fiche d'un professeur (F4).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table professeur avec ses coordonnées.
 * IDENTITÉ     : email (unique).
 * UTILISÉE PAR : ProfesseurRepository, ProfesseurService.
 * NE FAIT PAS  : de logique métier ni de gestion des matières (via enseignement).
 */
@Entity
@Table(name = "professeur")
public class Professeur {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_professeur")
    private Integer idProfesseur;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Professeur() {
    }

    /** Crée un professeur avec ses coordonnées. */
    public Professeur(String nom, String prenom, String email, String telephone) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = telephone;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdProfesseur() { return idProfesseur; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }


    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Modifie les coordonnées d'un professeur (F4).
     */

    public void modifier(String email, String telephone) {
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = telephone;
    }
}

package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Un locataire (F4).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la table locataire sous forme d'objet Java.
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "locataire")
public class Locataire {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_locataire")
    private Integer idLocataire;

    /** Nom du locataire. */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /** Prénom : obligatoire en base, transmis vide par l'API quand il n'est pas renseigné. */
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    /** Email unique (contrainte locataire_email_key). */
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    /** Téléphone, rendu obligatoire par V7 (E-07). */
    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Locataire() ──────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Locataire() {
    }

    // ── Locataire(String, String, String, String) ────────────────────────

    /** Crée un locataire valide. */
    public Locataire(String nom, String prenom, String email, String telephone) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = prenom == null ? "" : prenom;
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = Objects.requireNonNull(telephone, "telephone");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdLocataire() { return idLocataire; }

    /** Retourne le nom. */
    public String getNom() { return nom; }

    /** Retourne le prénom. */
    public String getPrenom() { return prenom; }

    /** Retourne l'email. */
    public String getEmail() { return email; }

    /** Retourne le téléphone. */
    public String getTelephone() { return telephone; }

    // ── modifier() ─────────────────────────────────────────────────────────

    /** Met à jour les coordonnées, modifiables à tout moment (F4). */
    public void modifier(String nom, String prenom, String email, String telephone) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = prenom == null ? "" : prenom;
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = Objects.requireNonNull(telephone, "telephone");
    }
}

package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Client identifié par son email.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Client sous forme d’objet Java.
 * IDENTITÉ     : email.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "client")
public class Client {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Integer idClient;
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Client() ─────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Client() {}

    // ── Client(String nom, String prenom, String email) ──────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Client(String nom, String prenom, String email) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdClient() ────────────────────────────────────────────────────

    /** Retourne idClient. */
    public Integer getIdClient() { return idClient; }

    // ── getNom() ─────────────────────────────────────────────────────────

    /** Retourne nom. */
    public String getNom() { return nom; }

    // ── getPrenom() ──────────────────────────────────────────────────────

    /** Retourne prenom. */
    public String getPrenom() { return prenom; }

    // ── getEmail() ───────────────────────────────────────────────────────

    /** Retourne email. */
    public String getEmail() { return email; }
    public void modifier(String nom, String prenom, String email) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
    }
}

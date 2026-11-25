package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Technicien d'une équipe (F2). L'agence est déduite de l'équipe (A-01).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idTechnicien.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "technicien")
public class Technicien {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_technicien")
    private Integer idTechnicien;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "id_equipe", nullable = false)
    private Integer idEquipe;

    @Column(name = "matricule", length = 50)
    private String matricule;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Technicien() {
    }

    public Technicien(String nom, String prenom, String email, String telephone,
                      Integer idEquipe, String matricule) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = telephone;
        this.idEquipe = Objects.requireNonNull(idEquipe, "idEquipe");
        this.matricule = Objects.requireNonNull(matricule, "matricule");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdTechnicien() { return idTechnicien; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public Integer getIdEquipe() { return idEquipe; }
    public String getMatricule() { return matricule; }

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(String nom, String prenom, String email, String telephone, Integer idEquipe) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = telephone;
        this.idEquipe = Objects.requireNonNull(idEquipe, "idEquipe");
    }
}

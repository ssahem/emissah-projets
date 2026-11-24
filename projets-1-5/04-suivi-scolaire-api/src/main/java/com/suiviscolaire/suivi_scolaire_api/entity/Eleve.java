package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Fiche d'un élève (F2).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table eleve ; la classe vient de l'inscription
 *                courante.
 * IDENTITÉ     : le nom et le prénom ne suffisent pas : l'identité technique est idEleve.
 * UTILISÉE PAR : EleveRepository, EleveService.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "eleve")
public class Eleve {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_eleve")
    private Integer idEleve;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Eleve() {
    }

    /** Crée une fiche élève ; la date de naissance est validée par le service. */
    public Eleve(String nom, String prenom, LocalDate dateNaissance) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.dateNaissance = Objects.requireNonNull(dateNaissance, "dateNaissance");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdEleve() { return idEleve; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }

    /** Modifie le nom et le prénom ; la date de naissance reste figée (F2). */
    public void modifier(String nom, String prenom) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
    }
}

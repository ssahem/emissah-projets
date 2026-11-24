package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Affectation d'une matière à un professeur dans une classe (F12).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table enseignement, clé de rattachement des
 *                notes.
 * IDENTITÉ     : (idClasse, idMatiere) — un seul professeur par matière et par classe.
 * UTILISÉE PAR : EnseignementRepository, NoteService, ReglesMetierService.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "enseignement")
public class Enseignement {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enseignement")
    private Integer idEnseignement;

    @Column(name = "id_classe", nullable = false)
    private Integer idClasse;

    @Column(name = "id_matiere", nullable = false)
    private Integer idMatiere;

    @Column(name = "id_professeur", nullable = false)
    private Integer idProfesseur;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Enseignement() {
    }

    /** Crée un enseignement ; l'unicité est contrôlée par le service. */
    public Enseignement(Integer idClasse, Integer idMatiere, Integer idProfesseur) {
        this.idClasse = Objects.requireNonNull(idClasse, "idClasse");
        this.idMatiere = Objects.requireNonNull(idMatiere, "idMatiere");
        this.idProfesseur = Objects.requireNonNull(idProfesseur, "idProfesseur");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdEnseignement() { return idEnseignement; }
    public Integer getIdClasse() { return idClasse; }
    public Integer getIdMatiere() { return idMatiere; }
    public Integer getIdProfesseur() { return idProfesseur; }
}

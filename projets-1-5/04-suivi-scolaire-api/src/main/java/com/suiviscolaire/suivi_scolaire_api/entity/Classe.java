package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Classe d'une année scolaire (F1).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table classe.
 * IDENTITÉ     : (nom, idAnnee, idNiveau) — clé métier : une classe est unique par année et
 *                niveau.
 * UTILISÉE PAR : ClasseRepository, ClasseService.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "classe")
public class Classe {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_classe")
    private Integer idClasse;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "id_annee", nullable = false)
    private Integer idAnnee;

    @Column(name = "id_niveau", nullable = false)
    private Integer idNiveau;

    @Column(name = "effectif_max")
    private Integer effectifMax;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Classe() {
    }

    /** Crée une classe valide ; les champs obligatoires ne peuvent pas être null. */
    public Classe(String nom, Integer idAnnee, Integer idNiveau, Integer effectifMax) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.idAnnee = Objects.requireNonNull(idAnnee, "idAnnee");
        this.idNiveau = Objects.requireNonNull(idNiveau, "idNiveau");
        this.effectifMax = Objects.requireNonNull(effectifMax, "effectifMax");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdClasse() { return idClasse; }
    public String getNom() { return nom; }
    public Integer getIdAnnee() { return idAnnee; }
    public Integer getIdNiveau() { return idNiveau; }
    public Integer getEffectifMax() { return effectifMax; }


    // ── modifierEffectifMax() ────────────────────────────────────────────────

    /**
     * Modifie la capacité d'accueil (contrôle de non-réduction fait par le
     * service).
     */

    public void modifierEffectifMax(Integer effectifMax) {
        this.effectifMax = Objects.requireNonNull(effectifMax, "effectifMax");
    }
}

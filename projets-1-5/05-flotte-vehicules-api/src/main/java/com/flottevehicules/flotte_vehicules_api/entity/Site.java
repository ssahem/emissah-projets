package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Site de l'entreprise (F1).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code site}.
 * IDENTITÉ     : le nom, unique depuis V2 (l'id reste la clé technique).
 * UTILISÉ PAR  : SiteRepository, SiteService.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP.
 */
@Entity
@Table(name = "site")
public class Site {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_site")
    private Integer idSite;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "ville")
    private String ville;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Site() {
    }

    /** Crée un site avec ses champs obligatoires. */
    public Site(String nom, String ville, String adresse) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdSite() { return idSite; }

    /** Retourne le nom. */
    public String getNom() { return nom; }

    /** Retourne l'adresse. */
    public String getAdresse() { return adresse; }

    /** Retourne la ville. */
    public String getVille() { return ville; }

    // ═════════════════════════════════════════════════════════════════════════
    //  COMPORTEMENT
    // ═════════════════════════════════════════════════════════════════════════


    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Modifie l'adresse et la ville ; le nom est figé (F1).
     */

    public void modifier(String adresse, String ville) {
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.ville = Objects.requireNonNull(ville, "ville");
    }


    // ── equals() ─────────────────────────────────────────────────────────────

    /**
     * Égalité sur la clé métier (le nom), jamais sur l'id null avant
     * enregistrement.
     */

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Site site) || idSite == null) {
            return false;
        }
        return idSite.equals(site.idSite);
    }


    // ── hashCode() ───────────────────────────────────────────────────────────

    /**
     * Cohérent avec equals().
     */

    @Override
    public int hashCode() {
        return Objects.hashCode(idSite);
    }
}

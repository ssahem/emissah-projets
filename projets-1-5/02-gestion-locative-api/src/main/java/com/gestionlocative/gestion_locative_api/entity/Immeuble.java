package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Un immeuble du parc locatif (F2).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la table immeuble après V2 et V3.
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "immeuble")
public class Immeuble {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_immeuble")
    private Integer idImmeuble;

    /** Adresse principale. */
    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    /** Code postal. */
    @Column(name = "code_postal", nullable = false, length = 10)
    private String codePostal;

    /** Ville. */
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    /** Propriétaire : lien direct introduit par V2 (E-01). */
    @Column(name = "id_proprietaire", nullable = false)
    private Integer idProprietaire;

    /** Nombre d'étages : colonne introduite par V3 (E-02). */
    @Column(name = "nb_etages", nullable = false)
    private Integer nbEtages;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Immeuble() ───────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Immeuble() {
    }

    // ── Immeuble(String, String, String, Integer, Integer) ───────────────────

    /** Crée un immeuble valide ; le nombre d'étages doit être positif. */
    public Immeuble(String adresse, String codePostal, String ville,
                    Integer idProprietaire, Integer nbEtages) {
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.codePostal = Objects.requireNonNull(codePostal, "codePostal");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.idProprietaire = Objects.requireNonNull(idProprietaire, "idProprietaire");
        this.nbEtages = Objects.requireNonNull(nbEtages, "nbEtages");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdImmeuble() { return idImmeuble; }

    /** Retourne l'adresse. */
    public String getAdresse() { return adresse; }

    /** Retourne le code postal. */
    public String getCodePostal() { return codePostal; }

    /** Retourne la ville. */
    public String getVille() { return ville; }

    /** Retourne l'identifiant du propriétaire. */
    public Integer getIdProprietaire() { return idProprietaire; }

    /** Retourne le nombre d'étages. */
    public Integer getNbEtages() { return nbEtages; }

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Met à jour l'adresse, le nombre d'étages et le propriétaire (F2).
     */

    public void modifier(String adresse, String codePostal, String ville,
                         Integer idProprietaire, Integer nbEtages) {
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.codePostal = Objects.requireNonNull(codePostal, "codePostal");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.idProprietaire = Objects.requireNonNull(idProprietaire, "idProprietaire");
        this.nbEtages = Objects.requireNonNull(nbEtages, "nbEtages");
    }
}

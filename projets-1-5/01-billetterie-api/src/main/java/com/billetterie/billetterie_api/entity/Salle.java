package com.billetterie.billetterie_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Une salle de spectacle.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code salle}.
 * IDENTITÉ     : (nom, ville) ; l'identifiant technique est généré par la base.
 * UTILISÉE PAR : SalleRepository, SalleMapper, Place.
 * NE FAIT PAS  : de logique HTTP ni de validation métier complexe.
 */
@Entity
@Table(name = "salle")
public class Salle {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salle")
    private Integer idSalle;

    /** Nom de la salle. */
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /** Adresse postale, conservée pour l'affichage. */
    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    /** Ville, seconde composante de la clé métier (V3). */
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    /** Nombre maximal de places. */
    @Column(name = "capacite", nullable = false)
    private Integer capacite;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Salle() {
    }

    /** Crée une salle valide ; aucun champ obligatoire ne peut être null. */
    public Salle(String nom, String adresse, String ville, Integer capacite) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.capacite = Objects.requireNonNull(capacite, "capacite");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdSalle() { return idSalle; }

    /** Retourne le nom. */
    public String getNom() { return nom; }

    /** Retourne l'adresse. */
    public String getAdresse() { return adresse; }

    /** Retourne la ville. */
    public String getVille() { return ville; }

    /** Retourne la capacité. */
    public Integer getCapacite() { return capacite; }

    /** Met à jour les champs modifiables. */
    public void modifier(String nom, String adresse, String ville, Integer capacite) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.ville = Objects.requireNonNull(ville, "ville");
        this.capacite = Objects.requireNonNull(capacite, "capacite");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  IDENTITÉ ET AFFICHAGE
    // ═════════════════════════════════════════════════════════════════════════

    /** Égalité sur (nom, ville), la clé métier. */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Salle other)) { return false; }
        return Objects.equals(nom, other.nom) && Objects.equals(ville, other.ville);
    }

    /** Cohérent avec equals(). */
    @Override
    public int hashCode() { return Objects.hash(nom, ville); }
}

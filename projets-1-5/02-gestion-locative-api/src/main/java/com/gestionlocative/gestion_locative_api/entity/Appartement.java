package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Un appartement rattaché à un immeuble (F3).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la table appartement après V2 (plus de id_proprietaire).
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "appartement")
public class Appartement {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_appartement")
    private Integer idAppartement;

    /** Numéro, unique par immeuble. */
    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    /** Surface en m², strictement positive. */
    @Column(name = "surface", nullable = false, precision = 7, scale = 2)
    private BigDecimal surface;

    /** Nombre de pièces, strictement positif. */
    @Column(name = "nb_pieces", nullable = false)
    private Integer nbPieces;

    /** Étage, au plus égal au nombre d'étages de l'immeuble. */
    @Column(name = "etage", nullable = false)
    private Integer etage;

    /** Immeuble : figé après création (F3). */
    @Column(name = "id_immeuble", nullable = false)
    private Integer idImmeuble;

    /** Date de mise en service (V8), borne initiale de la vacance F18. */
    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Appartement() ────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Appartement() {
    }

    // ── Appartement(String, BigDecimal, Integer, Integer, Integer) ───────────

    /** Crée un appartement valide, sans date de service (usage des tests unitaires). */
    public Appartement(String numero, BigDecimal surface, Integer nbPieces,
                       Integer etage, Integer idImmeuble) {
        this(numero, surface, nbPieces, etage, idImmeuble, null);
    }

    /** Crée un appartement valide avec sa date de mise en service (V8). */
    public Appartement(String numero, BigDecimal surface, Integer nbPieces,
                       Integer etage, Integer idImmeuble, LocalDate dateCreation) {
        this.numero = Objects.requireNonNull(numero, "numero");
        this.surface = Objects.requireNonNull(surface, "surface");
        this.nbPieces = Objects.requireNonNull(nbPieces, "nbPieces");
        this.etage = Objects.requireNonNull(etage, "etage");
        this.idImmeuble = Objects.requireNonNull(idImmeuble, "idImmeuble");
        this.dateCreation = dateCreation;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdAppartement() { return idAppartement; }

    /** Retourne le numéro. */
    public String getNumero() { return numero; }

    /** Retourne la surface. */
    public BigDecimal getSurface() { return surface; }

    /** Retourne le nombre de pièces. */
    public Integer getNbPieces() { return nbPieces; }

    /** Retourne l'étage. */
    public Integer getEtage() { return etage; }

    /** Retourne l'identifiant de l'immeuble. */
    public Integer getIdImmeuble() { return idImmeuble; }

    /** Retourne la date de mise en service (V8). */
    public LocalDate getDateCreation() { return dateCreation; }

    // ── modifier() ───────────────────────────────────────────────────────────

    /** Met à jour surface et nombre de pièces ; l'immeuble reste figé (F3). */
    public void modifier(BigDecimal surface, Integer nbPieces) {
        this.surface = Objects.requireNonNull(surface, "surface");
        this.nbPieces = Objects.requireNonNull(nbPieces, "nbPieces");
    }
}

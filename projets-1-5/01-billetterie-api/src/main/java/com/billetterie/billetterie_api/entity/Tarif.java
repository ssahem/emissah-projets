package com.billetterie.billetterie_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Prix d'une catégorie pour un événement.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Tarif sous forme d’objet Java.
 * IDENTITÉ     : (événement, categorie).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "tarif")
public class Tarif {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarif")
    private Integer idTarif;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;
    @Column(name = "categorie", nullable = false, length = 10)
    private String categorie;
    @Column(name = "prix", nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Tarif() ──────────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Tarif() {}

    // ── Tarif(Evenement evenement, String categorie, BigDecimal prix) ────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Tarif(Evenement evenement, String categorie, BigDecimal prix) {
        this.evenement = Objects.requireNonNull(evenement, "evenement");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
        this.prix = Objects.requireNonNull(prix, "prix");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdTarif() ─────────────────────────────────────────────────────────

    /**
     * Retourne idTarif.
     */

    public Integer getIdTarif() { return idTarif; }

    // ── getEvenement() ───────────────────────────────────────────────────────

    /**
     * Retourne evenement.
     */

    public Evenement getEvenement() { return evenement; }

    // ── getCategorie() ───────────────────────────────────────────────────────

    /**
     * Retourne categorie.
     */

    public String getCategorie() { return categorie; }

    // ── getPrix() ────────────────────────────────────────────────────────────

    /**
     * Retourne prix.
     */

    public BigDecimal getPrix() { return prix; }
}

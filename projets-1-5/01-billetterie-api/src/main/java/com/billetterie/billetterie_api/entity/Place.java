package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Place physique dans une salle.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Place sous forme d’objet Java.
 * IDENTITÉ     : (salle, rang, numero).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "place")
public class Place {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_place")
    private Integer idPlace;
    @Column(name = "rang", nullable = false, length = 5)
    private String rang;
    @Column(name = "numero", nullable = false)
    private Integer numero;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_salle", nullable = false)
    private Salle salle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Place() ──────────────────────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    protected Place() {}
    // ── Place() ──────────────────────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Place(String rang, Integer numero, Salle salle, Categorie categorie) {
        this.rang = Objects.requireNonNull(rang, "rang");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.salle = Objects.requireNonNull(salle, "salle");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdPlace() ─────────────────────────────────────────────────────

    /** Retourne idPlace. */
    public Integer getIdPlace() { return idPlace; }
    // ── getRang() ────────────────────────────────────────────────────────

    /** Retourne rang. */
    public String getRang() { return rang; }
    // ── getNumero() ──────────────────────────────────────────────────────

    /** Retourne numero. */
    public Integer getNumero() { return numero; }
    // ── getSalle() ───────────────────────────────────────────────────────

    /** Retourne salle. */
    public Salle getSalle() { return salle; }
    // ── getCategorie() ───────────────────────────────────────────────────

    /** Retourne categorie. */
    public Categorie getCategorie() { return categorie; }

    /** Applique une modification complète de la place (F2). */
    public void modifier(String rang, Integer numero, Salle salle, Categorie categorie) {
        this.rang = Objects.requireNonNull(rang, "rang");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.salle = Objects.requireNonNull(salle, "salle");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
    }
}

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
 * Lien entre une réservation et la place (rang + numéro) concernée.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de ReservationPlace sous forme d’objet Java.
 * IDENTITÉ     : (événement, rang, numero).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "reservation_place")
public class ReservationPlace {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation_place")
    private Integer idReservationPlace;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_reservation", nullable = false)
    private Reservation reservation;

    @Column(name = "rang", length = 5)
    private String rang;

    @Column(name = "numero")
    private Integer numero;

    /** Événement dénormalisé (V4) : porte l'unicité (événement, place). */
    @Column(name = "id_evenement")
    private Integer idEvenement;

    @Column(name = "prix_applique", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixApplique = BigDecimal.ZERO;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── ReservationPlace() ───────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected ReservationPlace() {}

    // ── ReservationPlace(Reservation reservation, String rang, Integer nume… ─

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public ReservationPlace(Reservation reservation, String rang, Integer numero, Integer idEvenement) {
        this.reservation = Objects.requireNonNull(reservation, "reservation");
        this.rang = rang;
        this.numero = numero;
        this.idEvenement = idEvenement;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdReservationPlace() ──────────────────────────────────────────────

    /** Retourne idReservationPlace. */
    public Integer getIdReservationPlace() { return idReservationPlace; }

    // ── getReservation() ─────────────────────────────────────────────────────

    /** Retourne reservation. */
    public Reservation getReservation() { return reservation; }

    // ── getRang() ────────────────────────────────────────────────────────────

    /** Retourne rang. */
    public String getRang() { return rang; }

    // ── getNumero() ──────────────────────────────────────────────────────────

    /** Retourne numero. */
    public Integer getNumero() { return numero; }

    // ── getIdEvenement() ─────────────────────────────────────────────────────

    /** Retourne idEvenement. */
    public Integer getIdEvenement() { return idEvenement; }

    // ── getPrixApplique() ────────────────────────────────────────────────────

    /** Retourne prixApplique. */
    public BigDecimal getPrixApplique() { return prixApplique; }

    // ── setPrixApplique() ────────────────────────────────────────────────────

    /** Enregistre le prix appliqué à la place. */
    public void setPrixApplique(BigDecimal prixApplique) {
        this.prixApplique = Objects.requireNonNull(prixApplique, "prixApplique");
    }
}

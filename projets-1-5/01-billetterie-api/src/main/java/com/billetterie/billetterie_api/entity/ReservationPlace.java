package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/** Lien entre une réservation et la place (rang + numéro) concernée.
 * COUCHE : Entité · IDENTITÉ : (événement, rang, numero) · NE FAIT PAS : la vérification de disponibilité. */
@Entity
@Table(name = "reservation_place")
public class ReservationPlace {
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

    protected ReservationPlace() {}
    public ReservationPlace(Reservation reservation, String rang, Integer numero, Integer idEvenement) {
        this.reservation = Objects.requireNonNull(reservation, "reservation");
        this.rang = rang;
        this.numero = numero;
        this.idEvenement = idEvenement;
    }
    public Integer getIdReservationPlace() { return idReservationPlace; }
    public Reservation getReservation() { return reservation; }
    public String getRang() { return rang; }
    public Integer getNumero() { return numero; }
    public Integer getIdEvenement() { return idEvenement; }
    public BigDecimal getPrixApplique() { return prixApplique; }
    public void setPrixApplique(BigDecimal prixApplique) {
        this.prixApplique = Objects.requireNonNull(prixApplique, "prixApplique");
    }
}

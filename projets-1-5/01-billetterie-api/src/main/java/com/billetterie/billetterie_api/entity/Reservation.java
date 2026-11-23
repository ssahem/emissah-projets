package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Réservation d'une ou plusieurs places pour un événement.
 * COUCHE : Entité · IDENTITÉ : (client, événement, dateCreation) · NE FAIT PAS : les règles métier. */
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Integer idReservation;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    /** Échéance calculée à la création : date_creation + 15 minutes. */
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "montant_rembourse", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantRembourse = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationPlace> places = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueStatut> historique = new ArrayList<>();

    protected Reservation() {}
    public Reservation(LocalDateTime dateCreation, Client client, Evenement evenement) {
        this.dateCreation = Objects.requireNonNull(dateCreation, "dateCreation");
        this.dateExpiration = dateCreation.plusMinutes(15);
        this.client = Objects.requireNonNull(client, "client");
        this.evenement = Objects.requireNonNull(evenement, "evenement");
    }
    public Integer getIdReservation() { return idReservation; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public LocalDateTime getDatePaiement() { return datePaiement; }
    public BigDecimal getMontantRembourse() { return montantRembourse; }
    public Client getClient() { return client; }
    public Evenement getEvenement() { return evenement; }
    public List<ReservationPlace> getPlaces() { return places; }
    public List<HistoriqueStatut> getHistorique() { return historique; }
    public void ajouterPlace(ReservationPlace place) { places.add(place); }
    public void ajouterHistorique(HistoriqueStatut entree) { historique.add(entree); }
    public void enregistrerPaiement(LocalDateTime datePaiement) {
        this.datePaiement = Objects.requireNonNull(datePaiement, "datePaiement");
    }
    public void setMontantRembourse(BigDecimal montantRembourse) {
        this.montantRembourse = Objects.requireNonNull(montantRembourse, "montantRembourse");
    }
}

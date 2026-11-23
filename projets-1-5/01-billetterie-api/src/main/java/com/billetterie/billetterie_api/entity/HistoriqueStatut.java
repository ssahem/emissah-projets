package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Trace du changement de statut d'une réservation.
 * COUCHE : Entité · IDENTITÉ : (réservation, statut) · NE FAIT PAS : le calcul de remboursement. */
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {
    @EmbeddedId
    private HistoriqueStatutId id;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("idReservation")
    @JoinColumn(name = "id_reservation")
    private Reservation reservation;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("idStatut")
    @JoinColumn(name = "id_statut")
    private Statut statut;
    @Column(name = "date_statut", nullable = false)
    private LocalDateTime dateStatut;
    protected HistoriqueStatut() {}
    public HistoriqueStatut(Reservation reservation, Statut statut, LocalDateTime dateStatut) {
        this.reservation = reservation;
        this.statut = statut;
        this.dateStatut = dateStatut;
        this.id = new HistoriqueStatutId(reservation.getIdReservation(), statut.getIdStatut());
    }
    public HistoriqueStatutId getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public Statut getStatut() { return statut; }
    public LocalDateTime getDateStatut() { return dateStatut; }
}

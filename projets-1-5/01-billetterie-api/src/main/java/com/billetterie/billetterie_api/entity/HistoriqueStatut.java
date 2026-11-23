package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Trace du changement de statut d'une réservation.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de HistoriqueStatut sous forme d’objet Java.
 * IDENTITÉ     : (réservation, statut).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

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

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── HistoriqueStatut() ───────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected HistoriqueStatut() {}

    // ── HistoriqueStatut(Reservation reservation, Statut statut, LocalDateTime dateStatut) ─

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public HistoriqueStatut(Reservation reservation, Statut statut, LocalDateTime dateStatut) {
        this.reservation = reservation;
        this.statut = statut;
        this.dateStatut = dateStatut;
        this.id = new HistoriqueStatutId(reservation.getIdReservation(), statut.getIdStatut());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getId() ──────────────────────────────────────────────────────────

    /** Retourne id. */
    public HistoriqueStatutId getId() { return id; }

    // ── getReservation() ─────────────────────────────────────────────────

    /** Retourne reservation. */
    public Reservation getReservation() { return reservation; }

    // ── getStatut() ──────────────────────────────────────────────────────

    /** Retourne statut. */
    public Statut getStatut() { return statut; }

    // ── getDateStatut() ──────────────────────────────────────────────────

    /** Retourne dateStatut. */
    public LocalDateTime getDateStatut() { return dateStatut; }
}

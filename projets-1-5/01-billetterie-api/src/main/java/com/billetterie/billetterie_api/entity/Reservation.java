package com.billetterie.billetterie_api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Réservation d'une ou plusieurs places pour un événement.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Reservation sous forme d’objet Java.
 * IDENTITÉ     : (client, événement, dateCreation).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

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

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Reservation() ────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Reservation() {}

    // ── Reservation(LocalDateTime dateCreation, Client client, Evenement ev… ─

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Reservation(LocalDateTime dateCreation, Client client, Evenement evenement) {
        this.dateCreation = Objects.requireNonNull(dateCreation, "dateCreation");
        this.dateExpiration = dateCreation.plusMinutes(15);
        this.client = Objects.requireNonNull(client, "client");
        this.evenement = Objects.requireNonNull(evenement, "evenement");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdReservation() ───────────────────────────────────────────────────

    /**
     * Retourne idReservation.
     */

    public Integer getIdReservation() { return idReservation; }

    // ── getDateCreation() ────────────────────────────────────────────────────

    /**
     * Retourne dateCreation.
     */

    public LocalDateTime getDateCreation() { return dateCreation; }

    // ── getDateExpiration() ──────────────────────────────────────────────────

    /**
     * Retourne dateExpiration.
     */

    public LocalDateTime getDateExpiration() { return dateExpiration; }

    // ── getDatePaiement() ────────────────────────────────────────────────────

    /**
     * Retourne datePaiement.
     */

    public LocalDateTime getDatePaiement() { return datePaiement; }

    // ── getMontantRembourse() ────────────────────────────────────────────────

    /**
     * Retourne montantRembourse.
     */

    public BigDecimal getMontantRembourse() { return montantRembourse; }

    // ── getClient() ──────────────────────────────────────────────────────────

    /**
     * Retourne client.
     */

    public Client getClient() { return client; }

    // ── getEvenement() ───────────────────────────────────────────────────────

    /**
     * Retourne evenement.
     */

    public Evenement getEvenement() { return evenement; }

    // ── getPlaces() ──────────────────────────────────────────────────────────

    /**
     * Retourne places.
     */

    public List<ReservationPlace> getPlaces() { return places; }

    // ── getHistorique() ──────────────────────────────────────────────────────

    /**
     * Retourne historique.
     */

    public List<HistoriqueStatut> getHistorique() { return historique; }

    // ── ajouterPlace() ───────────────────────────────────────────────────────

    /**
     * Ajoute une place réservée en maintenant la relation.
     */

    public void ajouterPlace(ReservationPlace place) { places.add(place); }

    // ── ajouterHistorique() ──────────────────────────────────────────────────

    /**
     * Ajoute une entrée à l'historique des statuts.
     */

    public void ajouterHistorique(HistoriqueStatut entree) { historique.add(entree); }

    // ── enregistrerPaiement() ────────────────────────────────────────────────

    /**
     * Enregistre la date de paiement de la réservation.
     */

    public void enregistrerPaiement(LocalDateTime datePaiement) {
        this.datePaiement = Objects.requireNonNull(datePaiement, "datePaiement");
    }

    // ── setMontantRembourse() ────────────────────────────────────────────────

    /**
     * Enregistre le montant remboursé.
     */

    public void setMontantRembourse(BigDecimal montantRembourse) {
        this.montantRembourse = Objects.requireNonNull(montantRembourse, "montantRembourse");
    }
}

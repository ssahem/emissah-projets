package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Représentation datée d'un spectacle dans une salle.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Evenement sous forme d’objet Java.
 * IDENTITÉ     : (spectacle, salle, dateHeure).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "evenement")
public class Evenement {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement")
    private Integer idEvenement;
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_spectacle", nullable = false)
    private Spectacle spectacle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_salle", nullable = false)
    private Salle salle;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Evenement() ──────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Evenement() {}

    // ── Evenement(LocalDateTime dateHeure, Spectacle spectacle, Salle salle) ─

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Evenement(LocalDateTime dateHeure, Spectacle spectacle, Salle salle) {
        this.dateHeure = Objects.requireNonNull(dateHeure, "dateHeure");
        this.spectacle = Objects.requireNonNull(spectacle, "spectacle");
        this.salle = Objects.requireNonNull(salle, "salle");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdEvenement() ─────────────────────────────────────────────────

    /** Retourne idEvenement. */
    public Integer getIdEvenement() { return idEvenement; }

    // ── getDateHeure() ───────────────────────────────────────────────────

    /** Retourne dateHeure. */
    public LocalDateTime getDateHeure() { return dateHeure; }

    // ── getSpectacle() ───────────────────────────────────────────────────

    /** Retourne spectacle. */
    public Spectacle getSpectacle() { return spectacle; }

    // ── getSalle() ───────────────────────────────────────────────────────

    /** Retourne salle. */
    public Salle getSalle() { return salle; }
    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = Objects.requireNonNull(dateHeure, "dateHeure");
    }
    public void setSpectacle(Spectacle spectacle) {
        this.spectacle = Objects.requireNonNull(spectacle, "spectacle");
    }
    public void setSalle(Salle salle) { this.salle = Objects.requireNonNull(salle, "salle"); }
}

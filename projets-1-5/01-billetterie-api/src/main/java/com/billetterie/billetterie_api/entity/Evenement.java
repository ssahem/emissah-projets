package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/** Représentation datée d'un spectacle dans une salle.
 * COUCHE : Entité · IDENTITÉ : (spectacle, salle, dateHeure) · NE FAIT PAS : le contrôle de date. */
@Entity
@Table(name = "evenement")
public class Evenement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement")
    private Integer idEvenement;
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_spectacle", nullable = false)
    private Spectacle spectacle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_salle", nullable = false)
    private Salle salle;
    protected Evenement() {}
    public Evenement(LocalDateTime dateHeure, Spectacle spectacle, Salle salle) {
        this.dateHeure = Objects.requireNonNull(dateHeure, "dateHeure");
        this.spectacle = Objects.requireNonNull(spectacle, "spectacle");
        this.salle = Objects.requireNonNull(salle, "salle");
    }
    public Integer getIdEvenement() { return idEvenement; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public Spectacle getSpectacle() { return spectacle; }
    public Salle getSalle() { return salle; }
    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = Objects.requireNonNull(dateHeure, "dateHeure");
    }
    public void setSpectacle(Spectacle spectacle) {
        this.spectacle = Objects.requireNonNull(spectacle, "spectacle");
    }
    public void setSalle(Salle salle) { this.salle = Objects.requireNonNull(salle, "salle"); }
}

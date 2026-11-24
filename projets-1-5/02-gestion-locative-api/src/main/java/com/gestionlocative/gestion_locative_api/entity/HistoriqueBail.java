package com.gestionlocative.gestion_locative_api.entity;

import com.gestionlocative.gestion_locative_api.enums.MotifHistoriqueBail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Événement du cycle de vie d'un bail (migration V5).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : tracer les résiliations (F13), renouvellements (F14) et
 *                clôtures (F16) avec la date de fin avant et après l'action.
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : HistoriqueBailRepository, BailService et BailClotureService.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "historique_bail")
public class HistoriqueBail {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;

    /** Bail concerné. */
    @Column(name = "id_bail", nullable = false)
    private Integer idBail;

    /** Nature de l'événement. */
    @Column(name = "motif", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MotifHistoriqueBail motif;

    /** Date de l'événement, fournie par l'horloge applicative. */
    @Column(name = "date_evenement", nullable = false)
    private LocalDate dateEvenement;

    /** Date de fin avant l'événement, quand il en existe une. */
    @Column(name = "date_fin_avant")
    private LocalDate dateFinAvant;

    /** Date de fin après l'événement. */
    @Column(name = "date_fin_apres")
    private LocalDate dateFinApres;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── HistoriqueBail() ────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected HistoriqueBail() {
    }

    // ── HistoriqueBail(Integer, MotifHistoriqueBail, LocalDate, LocalDate, LocalDate) ──

    /** Crée un événement de cycle de vie. */
    public HistoriqueBail(Integer idBail, MotifHistoriqueBail motif, LocalDate dateEvenement,
                          LocalDate dateFinAvant, LocalDate dateFinApres) {
        this.idBail = Objects.requireNonNull(idBail, "idBail");
        this.motif = Objects.requireNonNull(motif, "motif");
        this.dateEvenement = Objects.requireNonNull(dateEvenement, "dateEvenement");
        this.dateFinAvant = dateFinAvant;
        this.dateFinApres = dateFinApres;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdHistorique() { return idHistorique; }

    /** Retourne l'identifiant du bail. */
    public Integer getIdBail() { return idBail; }

    /** Retourne le motif. */
    public MotifHistoriqueBail getMotif() { return motif; }

    /** Retourne la date de l'événement. */
    public LocalDate getDateEvenement() { return dateEvenement; }

    /** Retourne l'ancienne date de fin. */
    public LocalDate getDateFinAvant() { return dateFinAvant; }

    /** Retourne la nouvelle date de fin. */
    public LocalDate getDateFinApres() { return dateFinApres; }
}

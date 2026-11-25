package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Trimestre d'une année scolaire (F5, F13, F14, F15).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : porte l'état de clôture qui fige les notes du trimestre.
 * IDENTITÉ     : (numero, idAnnee) — les notes référencent le numéro, pas l'identifiant.
 * UTILISÉE PAR : TrimestreRepository, NoteService, ReglesMetierService,
 *                AlertesDifficulteService.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "trimestre")
public class Trimestre {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trimestre")
    private Integer idTrimestre;

    @Column(name = "numero", nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer numero;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "id_annee", nullable = false)
    private Integer idAnnee;

    @Column(name = "est_cloture", nullable = false)
    private boolean estCloture;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Trimestre() {
    }

    /** Crée un trimestre ouvert. */
    public Trimestre(Integer numero, LocalDate dateDebut, LocalDate dateFin, Integer idAnnee) {
        this.numero = Objects.requireNonNull(numero, "numero");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
        this.dateFin = Objects.requireNonNull(dateFin, "dateFin");
        this.idAnnee = Objects.requireNonNull(idAnnee, "idAnnee");
        this.estCloture = false;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdTrimestre() { return idTrimestre; }
    public Integer getNumero() { return numero; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public Integer getIdAnnee() { return idAnnee; }
    public boolean isEstCloture() { return estCloture; }
    public LocalDate getDateCloture() { return dateCloture; }

    /** Clôture le trimestre à la date donnée (F14). */

    // ── cloturer() ───────────────────────────────────────────────────────────

    /**
     * Exécute cloturer().
     */

    public void cloturer(LocalDate dateCloture) {
        this.estCloture = true;
        this.dateCloture = Objects.requireNonNull(dateCloture, "dateCloture");
    }

    /** Rouvre le trimestre (F15) ; la date de clôture est effacée mais l'historique subsiste. */

    // ── reouvrir() ───────────────────────────────────────────────────────────

    /**
     * Exécute reouvrir().
     */

    public void reouvrir() {
        this.estCloture = false;
        this.dateCloture = null;
    }
}

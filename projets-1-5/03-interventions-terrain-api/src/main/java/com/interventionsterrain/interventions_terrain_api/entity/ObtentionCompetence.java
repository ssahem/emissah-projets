package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Compétence obtenue par un technicien (F3, F7, F16).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idObtention.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "obtention_competence")
public class ObtentionCompetence {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obtention")
    private Integer idObtention;

    @Column(name = "date_obtention", nullable = false)
    private LocalDate dateObtention;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "id_technicien", nullable = false)
    private Integer idTechnicien;

    @Column(name = "id_competence", nullable = false)
    private Integer idCompetence;

    @Column(name = "date_alerte")
    private LocalDate dateAlerte;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected ObtentionCompetence() {
    }

    public ObtentionCompetence(LocalDate dateObtention, LocalDate dateExpiration,
                               Integer idTechnicien, Integer idCompetence) {
        this.dateObtention = java.util.Objects.requireNonNull(dateObtention, "dateObtention");
        this.dateExpiration = dateExpiration;
        this.idTechnicien = java.util.Objects.requireNonNull(idTechnicien, "idTechnicien");
        this.idCompetence = java.util.Objects.requireNonNull(idCompetence, "idCompetence");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdObtention() { return idObtention; }
    public LocalDate getDateObtention() { return dateObtention; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public Integer getIdTechnicien() { return idTechnicien; }
    public Integer getIdCompetence() { return idCompetence; }
    public LocalDate getDateAlerte() { return dateAlerte; }

    /** Positionne la date de signalement pour ne jamais alerter deux fois (S-06, F16). */
    public void signaler(LocalDate date) { this.dateAlerte = date; }
}

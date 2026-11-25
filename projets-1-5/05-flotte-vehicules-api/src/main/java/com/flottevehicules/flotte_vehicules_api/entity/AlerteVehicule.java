package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Alerte produite par F16 ou par un conflit d'entretien (F12, décision A-02).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : tracer les alertes ; une seule FK, vers affectation.
 * IDENTITÉ     : le couple (véhicule, motif, date d'échéance), unique depuis V3.
 * UTILISÉ PAR  : AlerteRepository, AlerteService.
 * NE FAIT PAS  : de planification ni de sérialisation HTTP.
 */
@Entity
@Table(name = "alerte_vehicule")
public class AlerteVehicule {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Integer idAlerte;

    @Column(name = "id_affectation")
    private Integer idAffectation;

    @Column(name = "id_vehicule", nullable = false)
    private Integer idVehicule;

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "periode", nullable = false)
    private String periode;

    @Column(name = "date_alerte", nullable = false)
    private LocalDate dateAlerte;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Column(name = "ecart_km")
    private Integer ecartKm;

    @Column(name = "ecart_jours")
    private Integer ecartJours;

    @Column(name = "detail")
    private String detail;

    /** Constructeur vide exigé par JPA. */
    protected AlerteVehicule() {
    }

    /** Crée une alerte complète avec ses écarts constatés. */
    public AlerteVehicule(Integer idAffectation, Integer idVehicule, String motif, String periode,
                          LocalDate dateAlerte, LocalDate dateEcheance,
                          Integer ecartKm, Integer ecartJours, String detail) {
        this.idAffectation = idAffectation;
        this.idVehicule = Objects.requireNonNull(idVehicule, "idVehicule");
        this.motif = Objects.requireNonNull(motif, "motif");
        this.periode = Objects.requireNonNull(periode, "periode");
        this.dateAlerte = Objects.requireNonNull(dateAlerte, "dateAlerte");
        this.dateEcheance = Objects.requireNonNull(dateEcheance, "dateEcheance");
        this.ecartKm = ecartKm;
        this.ecartJours = ecartJours;
        this.detail = detail;
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdAlerte() { return idAlerte; }

    /** Retourne l'affectation éventuellement liée. */
    public Integer getIdAffectation() { return idAffectation; }

    /** Retourne le véhicule (colonne simple, sans FK). */
    public Integer getIdVehicule() { return idVehicule; }

    /** Retourne le motif. */
    public String getMotif() { return motif; }

    /** Retourne la période au format yyyy-MM. */
    public String getPeriode() { return periode; }

    /** Retourne la date d'émission. */
    public LocalDate getDateAlerte() { return dateAlerte; }

    /** Retourne la date d'échéance concernée. */
    public LocalDate getDateEcheance() { return dateEcheance; }

    /** Retourne l'écart kilométrique constaté. */
    public Integer getEcartKm() { return ecartKm; }

    /** Retourne l'écart en jours constaté. */
    public Integer getEcartJours() { return ecartJours; }

    /** Retourne le détail lisible. */
    public String getDetail() { return detail; }
}

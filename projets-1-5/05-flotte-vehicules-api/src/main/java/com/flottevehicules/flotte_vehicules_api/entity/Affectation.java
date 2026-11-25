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
 * Affectation d'un véhicule à un salarié (F4).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code affectation}, y compris le relevé de
 *                restitution ajouté en V2.
 * IDENTITÉ     : le couple (véhicule, salarié, date de début).
 * UTILISÉ PAR  : AffectationRepository, AffectationService, SiteService.
 * NE FAIT PAS  : de contrôle de chevauchement ni de sérialisation HTTP.
 */
@Entity
@Table(name = "affectation")
public class Affectation {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Integer idAffectation;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "id_vehicule", nullable = false)
    private Integer idVehicule;

    @Column(name = "id_salarie", nullable = false)
    private Integer idSalarie;

    @Column(name = "km_restitution")
    private Integer kmRestitution;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Affectation() {
    }

    /** Crée une affectation ; la date de fin reste facultative. */
    public Affectation(LocalDate dateDebut, LocalDate dateFin, Integer idVehicule, Integer idSalarie) {
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
        this.dateFin = dateFin;
        this.idVehicule = Objects.requireNonNull(idVehicule, "idVehicule");
        this.idSalarie = Objects.requireNonNull(idSalarie, "idSalarie");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdAffectation() { return idAffectation; }

    /** Retourne la date de début. */
    public LocalDate getDateDebut() { return dateDebut; }

    /** Retourne la date de fin, éventuellement nulle. */
    public LocalDate getDateFin() { return dateFin; }

    /** Retourne le véhicule concerné. */
    public Integer getIdVehicule() { return idVehicule; }

    /** Retourne le salarié concerné. */
    public Integer getIdSalarie() { return idSalarie; }

    /** Retourne le relevé kilométrique de restitution, éventuellement nul. */
    public Integer getKmRestitution() { return kmRestitution; }

    // ═════════════════════════════════════════════════════════════════════════
    //  COMPORTEMENT
    // ═════════════════════════════════════════════════════════════════════════


    // ── estCommencee() ───────────────────────────────────────────────────────

    /**
     * Indique si l'affectation a commencé à la date donnée.
     */

    public boolean estCommencee(LocalDate date) {
        return !date.isBefore(dateDebut);
    }


    // ── estEnCours() ─────────────────────────────────────────────────────────

    /**
     * Indique si l'affectation est en cours à la date donnée.
     */

    public boolean estEnCours(LocalDate date) {
        return estCommencee(date) && (dateFin == null || !date.isAfter(dateFin));
    }


    // ── clore() ──────────────────────────────────────────────────────────────

    /**
     * Clôture l'affectation avec une date de fin et un relevé kilométrique
     * (F13).
     */

    public void clore(LocalDate fin, Integer km) {
        this.dateFin = Objects.requireNonNull(fin, "dateFin");
        this.kmRestitution = Objects.requireNonNull(km, "kmRestitution");
    }


    // ── modifierDateFin() ────────────────────────────────────────────────────

    /**
     * Modifie uniquement la date de fin (F4).
     */

    public void modifierDateFin(LocalDate fin) {
        this.dateFin = fin;
    }


    // ── modifierParties() ────────────────────────────────────────────────────

    /**
     * Modifie véhicule et salarié tant que l'affectation n'a pas commencé (F4).
     */

    public void modifierParties(Integer vehicule, Integer salarie) {
        this.idVehicule = Objects.requireNonNull(vehicule, "idVehicule");
        this.idSalarie = Objects.requireNonNull(salarie, "idSalarie");
    }
}

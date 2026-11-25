package com.gestionlocative.gestion_locative_api.entity;

import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Un bail locatif (F5).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la table bail après V1 et V4 (statut, dates de
 *                resiliation et de clôture).
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "bail")
public class Bail {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bail")
    private Integer idBail;

    /** Date de début, obligatoire. */
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /** Date de fin, nulle pour un bail sans terme. */
    @Column(name = "date_fin")
    private LocalDate dateFin;

    /** Provision pour charges mensuelle. */
    @Column(name = "provision_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal provisionCharges;

    /** Dépôt de garantie. */
    @Column(name = "depot_garantie", nullable = false, precision = 12, scale = 2)
    private BigDecimal depotGarantie;

    /** Appartement loué, figé après création. */
    @Column(name = "id_appartement", nullable = false)
    private Integer idAppartement;

    /** Loyer courant ; les valeurs passées sont dans revision_loyer (V6). */
    @Column(name = "loyer_mensuel", nullable = false, precision = 12, scale = 2)
    private BigDecimal loyerMensuel;

    /** Cycle de vie introduit par V4 (E-04). */
    @Column(name = "statut", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatutBail statut;

    /** Date d'effet de la résiliation, renseignée par F13. */
    @Column(name = "date_resiliation")
    private LocalDate dateResiliation;

    /** Date de clôture, renseignée par F16. */
    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Bail() ───────────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Bail() {
    }

    // ── Bail(LocalDate, LocalDate, BigDecimal, BigDecimal, Integer, BigDeci… ─

    /** Crée un bail au statut initial EN_COURS. */
    public Bail(LocalDate dateDebut, LocalDate dateFin, BigDecimal provisionCharges,
                BigDecimal depotGarantie, Integer idAppartement, BigDecimal loyerMensuel) {
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
        this.dateFin = dateFin;
        this.provisionCharges = Objects.requireNonNull(provisionCharges, "provisionCharges");
        this.depotGarantie = Objects.requireNonNull(depotGarantie, "depotGarantie");
        this.idAppartement = Objects.requireNonNull(idAppartement, "idAppartement");
        this.loyerMensuel = Objects.requireNonNull(loyerMensuel, "loyerMensuel");
        this.statut = StatutBail.EN_COURS;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdBail() { return idBail; }

    /** Retourne la date de début. */
    public LocalDate getDateDebut() { return dateDebut; }

    /** Retourne la date de fin, éventuellement nulle. */
    public LocalDate getDateFin() { return dateFin; }

    /** Retourne la provision pour charges. */
    public BigDecimal getProvisionCharges() { return provisionCharges; }

    /** Retourne le dépôt de garantie. */
    public BigDecimal getDepotGarantie() { return depotGarantie; }

    /** Retourne l'identifiant de l'appartement. */
    public Integer getIdAppartement() { return idAppartement; }

    /** Retourne le loyer courant. */
    public BigDecimal getLoyerMensuel() { return loyerMensuel; }

    /** Retourne le statut. */
    public StatutBail getStatut() { return statut; }

    /** Retourne la date de résiliation, éventuellement nulle. */
    public LocalDate getDateResiliation() { return dateResiliation; }

    /** Retourne la date de clôture, éventuellement nulle. */
    public LocalDate getDateCloture() { return dateCloture; }

    // ── modifierAvantDebut() ─────────────────────────────────────────────────

    /**
     * Modifie loyer et date de fin avant le début du bail (F5).
     */

    public void modifierAvantDebut(BigDecimal loyerMensuel, LocalDate dateFin) {
        this.loyerMensuel = Objects.requireNonNull(loyerMensuel, "loyerMensuel");
        this.dateFin = dateFin;
    }

    // ── modifierDateFin() ────────────────────────────────────────────────────

    /**
     * Modifie la date de fin après le début du bail (F5).
     */

    public void modifierDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TRANSITIONS DU CYCLE DE VIE (F13–F16)
    // ═════════════════════════════════════════════════════════════════════════

    // ── resilier() ───────────────────────────────────────────────────────────

    /**
     * Résilie le bail à la date d'effet : la fin est ramenée à cette date
     * (F13). Les règles d'éligibilité restent dans le service.
     */

    public void resilier(LocalDate dateEffet) {
        this.statut = StatutBail.RESILIE;
        this.dateResiliation = Objects.requireNonNull(dateEffet, "dateEffet");
        this.dateFin = dateEffet;
    }

    // ── renouveler() ─────────────────────────────────────────────────────────

    /**
     * Prolonge l'échéance ; un bail clôturé redevient actif (F14).
     */

    public void renouveler(LocalDate nouvelleDateFin) {
        this.dateFin = Objects.requireNonNull(nouvelleDateFin, "nouvelleDateFin");
        if (this.statut == StatutBail.TERMINE) {
            this.statut = StatutBail.EN_COURS;
            this.dateCloture = null;
        }
    }

    // ── cloturer() ───────────────────────────────────────────────────────────

    /**
     * Clôture un bail échu sans modifier sa date de fin (F16).
     */

    public void cloturer(LocalDate dateCloture) {
        this.statut = StatutBail.TERMINE;
        this.dateCloture = Objects.requireNonNull(dateCloture, "dateCloture");
    }

    // ── appliquerLoyer() ─────────────────────────────────────────────────────

    /**
     * Remplace le loyer courant après une révision (F15).
     */

    public void appliquerLoyer(BigDecimal montant) {
        this.loyerMensuel = Objects.requireNonNull(montant, "montant");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ÉTAT À UNE DATE DONNÉE
    // ═════════════════════════════════════════════════════════════════════════

    // ── estEnCoursLe() ───────────────────────────────────────────────────────

    /**
     * Vrai si le bail occupe effectivement l'appartement à la date donnée (F6,
     * F8). Un bail sans date de fin reste en cours tant qu'il n'est pas
     * résilié.
     */

    public boolean estEnCoursLe(LocalDate date) {
        return statut == StatutBail.EN_COURS
                && !dateDebut.isAfter(date)
                && (dateFin == null || !dateFin.isBefore(date));
    }

    // ── estFuturLe() ─────────────────────────────────────────────────────────

    /**
     * Vrai si le bail est prévu mais n'a pas encore commencé à la date donnée
     * (F6).
     */

    public boolean estFuturLe(LocalDate date) {
        return statut == StatutBail.EN_COURS && dateDebut.isAfter(date);
    }

    // ── contient() ───────────────────────────────────────────────────────────

    /**
     * Vrai si la date appartient à la période contractuelle, indépendamment du
     * statut : utilisé pour borner une résiliation (F13) ou une révision (F15).
     */

    public boolean contient(LocalDate date) {
        return !dateDebut.isAfter(date) && (dateFin == null || !date.isAfter(dateFin));
    }
}

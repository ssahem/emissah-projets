package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Une révision de loyer (V6, E-06).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : conserve l'historique des loyers d'un bail ; la valeur courante
 *                reste dans {@link Bail#getLoyerMensuel()}.
 * IDENTITÉ     : identifiant technique généré par la base.
 * UTILISÉE PAR : RevisionLoyerRepository et BailService.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "revision_loyer")
public class RevisionLoyer {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant technique. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_revision")
    private Integer idRevision;

    /** Bail concerné. */
    @Column(name = "id_bail", nullable = false)
    private Integer idBail;

    /** Date de prise d'effet. */
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    /** Montant du loyer à cette date. */
    @Column(name = "montant", nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── RevisionLoyer() ──────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected RevisionLoyer() {
    }

    // ── RevisionLoyer(Integer, LocalDate, BigDecimal) ────────────────────────

    /** Crée la révision initiale d'un bail. */
    public RevisionLoyer(Integer idBail, LocalDate dateEffet, BigDecimal montant) {
        this.idBail = Objects.requireNonNull(idBail, "idBail");
        this.dateEffet = Objects.requireNonNull(dateEffet, "dateEffet");
        this.montant = Objects.requireNonNull(montant, "montant");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdRevision() { return idRevision; }

    /** Retourne l'identifiant du bail. */
    public Integer getIdBail() { return idBail; }

    /** Retourne la date d'effet. */
    public LocalDate getDateEffet() { return dateEffet; }

    /** Retourne le montant. */
    public BigDecimal getMontant() { return montant; }

    // ── modifierMontant() ────────────────────────────────────────────────────

    /**
     * Ajuste la révision initiale quand le loyer change avant le début du bail.
     */

    public void modifierMontant(BigDecimal montant) {
        this.montant = Objects.requireNonNull(montant, "montant");
    }
}

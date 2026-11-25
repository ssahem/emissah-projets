package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dépense rattachée à un véhicule.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code depense}, lue par les vues SQL.
 * IDENTITÉ     : l'identifiant technique.
 * UTILISÉ PAR  : DepenseRepository, vues SQL.
 * NE FAIT PAS  : de calcul de coût pour F18 (D-03 : entretiens uniquement).
 */
@Entity
@Table(name = "depense")
public class Depense {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_depense")
    private Integer idDepense;

    @Column(name = "date_depense", nullable = false)
    private LocalDate dateDepense;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "id_vehicule", nullable = false)
    private Integer idVehicule;

    @Column(name = "id_type_depense", nullable = false)
    private Integer idTypeDepense;

    /** Constructeur vide exigé par JPA. */
    protected Depense() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdDepense() { return idDepense; }

    /** Retourne la date. */
    public LocalDate getDateDepense() { return dateDepense; }

    /** Retourne le montant. */
    public BigDecimal getMontant() { return montant; }

    /** Retourne le commentaire. */
    public String getCommentaire() { return commentaire; }

    /** Retourne le véhicule. */
    public Integer getIdVehicule() { return idVehicule; }

    /** Retourne le type de dépense. */
    public Integer getIdTypeDepense() { return idTypeDepense; }
}

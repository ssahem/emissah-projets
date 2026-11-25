package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Type d'évaluation et son poids (F5, F13).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table type_evaluation.
 * IDENTITÉ     : libelle (unique).
 * UTILISÉE PAR : TypeEvaluationRepository, ReglesMetierService.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "type_evaluation")
public class TypeEvaluation {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_eval")
    private Integer idTypeEval;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "poids", nullable = false)
    private BigDecimal poids;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected TypeEvaluation() {
    }

    /** Crée un type d'évaluation avec un poids strictement positif. */
    public TypeEvaluation(String libelle, BigDecimal poids) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.poids = Objects.requireNonNull(poids, "poids");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdTypeEval() { return idTypeEval; }
    public String getLibelle() { return libelle; }
    public BigDecimal getPoids() { return poids; }


    // ── modifierPoids() ──────────────────────────────────────────────────────

    /**
     * Change le poids (F13) ; la validité est vérifiée par le service.
     */

    public void modifierPoids(BigDecimal poids) { this.poids = Objects.requireNonNull(poids, "poids"); }
}

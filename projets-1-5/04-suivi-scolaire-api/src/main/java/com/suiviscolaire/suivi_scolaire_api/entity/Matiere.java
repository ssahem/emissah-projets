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
 * Matière enseignée (F3).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table matiere, avec son coefficient global.
 * IDENTITÉ     : libelle (unique) et code (unique).
 * UTILISÉE PAR : MatiereRepository, MatiereService, services de restitution.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "matiere")
public class Matiere {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matiere")
    private Integer idMatiere;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "coefficient")
    private BigDecimal coefficient;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Matiere() {
    }

    /** Crée une matière valide ; le coefficient positif est vérifié par le service. */
    public Matiere(String libelle, String code, BigDecimal coefficient) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.code = Objects.requireNonNull(code, "code");
        this.coefficient = Objects.requireNonNull(coefficient, "coefficient");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdMatiere() { return idMatiere; }
    public String getLibelle() { return libelle; }
    public String getCode() { return code; }
    public BigDecimal getCoefficient() { return coefficient; }

    /** Modifie le libellé et le coefficient (F3). */

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(String libelle, BigDecimal coefficient) {
        this.libelle = Objects.requireNonNull(libelle, "libelle");
        this.coefficient = Objects.requireNonNull(coefficient, "coefficient");
    }
}

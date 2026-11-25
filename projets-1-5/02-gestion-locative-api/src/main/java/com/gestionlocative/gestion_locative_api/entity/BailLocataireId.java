package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clé primaire composite de la table de liaison bail ↔ locataire (E-08).
 *
 * COUCHE       : Entité (clé embarquée)
 * RÔLE         : porter le couple (id_bail, id_locataire) exigé par le schéma source.
 * IDENTITÉ     : les deux identifiants techniques.
 * UTILISÉE PAR : {@link BailLocataire}.
 * NE FAIT PAS  : de logique métier.
 */
@Embeddable
public class BailLocataireId implements Serializable {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Identifiant du bail. */
    @Column(name = "id_bail")
    private Integer idBail;

    /** Identifiant du locataire. */
    @Column(name = "id_locataire")
    private Integer idLocataire;

    // ── BailLocataireId() ────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected BailLocataireId() {
    }

    // ── BailLocataireId(Integer, Integer) ────────────────────────────────────

    /**
     * Crée la clé composite.
     */

    public BailLocataireId(Integer idBail, Integer idLocataire) {
        this.idBail = Objects.requireNonNull(idBail, "idBail");
        this.idLocataire = Objects.requireNonNull(idLocataire, "idLocataire");
    }

    public Integer getIdBail() { return idBail; }

    /** Retourne l'identifiant du locataire. */
    public Integer getIdLocataire() { return idLocataire; }

    // ── equals() ─────────────────────────────────────────────────────────────

    /**
     * Égalité sur les deux identifiants.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BailLocataireId other)) {
            return false;
        }
        return Objects.equals(idBail, other.idBail)
                && Objects.equals(idLocataire, other.idLocataire);
    }

    // ── hashCode() ───────────────────────────────────────────────────────────

    /**
     * Cohérent avec equals().
     */

    @Override
    public int hashCode() {
        return Objects.hash(idBail, idLocataire);
    }
}

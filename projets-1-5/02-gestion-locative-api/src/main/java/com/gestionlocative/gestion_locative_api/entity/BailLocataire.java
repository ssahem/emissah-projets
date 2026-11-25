package com.gestionlocative.gestion_locative_api.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Liaison entre un bail et un locataire (E-08).
 *
 * COUCHE       : Entité (table de liaison)
 * RÔLE         : conserve la clé composite du schéma source, tout en exposant un
 *                seul locataire principal côté API.
 * IDENTITÉ     : {@link BailLocataireId} (id_bail, id_locataire).
 * UTILISÉE PAR : BailLocataireRepository et BailService.
 * NE FAIT PAS  : de logique HTTP ni de JSON.
 */
@Entity
@Table(name = "bail_locataire")
public class BailLocataire {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Clé primaire composite. */
    @EmbeddedId
    private BailLocataireId id;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── BailLocataire() ──────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected BailLocataire() {
    }

    // ── BailLocataire(Integer, Integer) ──────────────────────────────────────

    /** Crée la liaison pour le locataire principal d'un bail. */
    public BailLocataire(Integer idBail, Integer idLocataire) {
        this.id = new BailLocataireId(idBail, idLocataire);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne la clé composite. */
    public BailLocataireId getId() { return id; }
}

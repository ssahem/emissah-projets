package com.billetterie.billetterie_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clé composite de historique_statut : (id_reservation, id_statut).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de HistoriqueStatutId sous forme d’objet Java.
 * IDENTITÉ     : l'identifiant technique.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Embeddable
public class HistoriqueStatutId implements Serializable {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Column(name = "id_reservation")
    private Integer idReservation;
    @Column(name = "id_statut")
    private Integer idStatut;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── HistoriqueStatutId() ─────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected HistoriqueStatutId() {}

    // ── HistoriqueStatutId(Integer idReservation, Integer idStatut) ──────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public HistoriqueStatutId(Integer idReservation, Integer idStatut) {
        this.idReservation = idReservation;
        this.idStatut = idStatut;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdReservation() ───────────────────────────────────────────────

    /** Retourne idReservation. */
    public Integer getIdReservation() { return idReservation; }

    // ── getIdStatut() ────────────────────────────────────────────────────

    /** Retourne idStatut. */
    public Integer getIdStatut() { return idStatut; }
    @Override

    // ═════════════════════════════════════════════════════════════════════════
    //  IDENTITÉ ET AFFICHAGE
    // ═════════════════════════════════════════════════════════════════════════

    // ── equals() ─────────────────────────────────────────────────────────

    /** Égalité sur la clé métier, pas sur l’identifiant technique. */
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof HistoriqueStatutId other)) { return false; }
        return Objects.equals(idReservation, other.idReservation) && Objects.equals(idStatut, other.idStatut);
    }
    @Override

    // ── hashCode() ───────────────────────────────────────────────────────

    /** Cohérent avec equals() : calculé sur la clé métier. */
    public int hashCode() { return Objects.hash(idReservation, idStatut); }
}

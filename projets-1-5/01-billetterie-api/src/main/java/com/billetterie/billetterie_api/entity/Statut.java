package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Statut d'une réservation (PROVISOIRE, PAYEE, ANNULEE).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Statut sous forme d’objet Java.
 * IDENTITÉ     : libelle.
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "statut")
public class Statut {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Integer idStatut;
    @Column(name = "libelle", nullable = false, length = 30)
    private String libelle;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Statut() ─────────────────────────────────────────────────────────

    /** Constructeur vide exigé par JPA. */
    protected Statut() {}

    // ── Statut(String libelle) ───────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Statut(String libelle) { this.libelle = Objects.requireNonNull(libelle, "libelle"); }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdStatut() ────────────────────────────────────────────────────

    /** Retourne idStatut. */
    public Integer getIdStatut() { return idStatut; }

    // ── getLibelle() ─────────────────────────────────────────────────────

    /** Retourne libelle. */
    public String getLibelle() { return libelle; }
}

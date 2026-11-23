package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import java.util.Objects;

/**
 * Spectacle programmé dans une salle.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter les données de Spectacle sous forme d’objet Java.
 * IDENTITÉ     : (titre, type).
 * UTILISÉE PAR : repositories, mappers et services.
 * NE FAIT PAS  : de logique HTTP ni de JSON (rôle du DTO).
 */
@Entity
@Table(name = "spectacle")
public class Spectacle {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_spectacle")
    private Integer idSpectacle;
    @Column(name = "titre", nullable = false, length = 150)
    private String titre;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, length = 20)
    private TypeSpectacle type;
    @Column(name = "placement_numerote", nullable = false)
    private Boolean placementNumerote;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── Spectacle() ──────────────────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    protected Spectacle() {}
    // ── Spectacle() ──────────────────────────────────────────────────────

    /** Crée une instance valide ; les champs obligatoires ne peuvent pas être null. */
    public Spectacle(String titre, TypeSpectacle type, Boolean placementNumerote) {
        this.titre = Objects.requireNonNull(titre, "titre");
        this.type = Objects.requireNonNull(type, "type");
        this.placementNumerote = Objects.requireNonNull(placementNumerote, "placementNumerote");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    // ── getIdSpectacle() ─────────────────────────────────────────────────

    /** Retourne idSpectacle. */
    public Integer getIdSpectacle() { return idSpectacle; }
    // ── getTitre() ───────────────────────────────────────────────────────

    /** Retourne titre. */
    public String getTitre() { return titre; }
    // ── getType() ────────────────────────────────────────────────────────

    /** Retourne type. */
    public TypeSpectacle getType() { return type; }
    // ── getPlacementNumerote() ───────────────────────────────────────────

    /** Retourne placementNumerote. */
    public Boolean getPlacementNumerote() { return placementNumerote; }
    public void setPlacementNumerote(Boolean placementNumerote) {
        this.placementNumerote = Objects.requireNonNull(placementNumerote, "placementNumerote");
    }
}

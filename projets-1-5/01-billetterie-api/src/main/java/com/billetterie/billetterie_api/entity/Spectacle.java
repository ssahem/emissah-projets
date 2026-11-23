package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import java.util.Objects;

/** Spectacle programmé dans une salle.
 * COUCHE : Entité · IDENTITÉ : (titre, type) · NE FAIT PAS : la programmation. */
@Entity
@Table(name = "spectacle")
public class Spectacle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_spectacle")
    private Integer idSpectacle;
    @Column(name = "titre", nullable = false, length = 150)
    private String titre;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, length = 20)
    private TypeSpectacle type;
    @Column(name = "placement_numerote", nullable = false)
    private Boolean placementNumerote;
    protected Spectacle() {}
    public Spectacle(String titre, TypeSpectacle type, Boolean placementNumerote) {
        this.titre = Objects.requireNonNull(titre, "titre");
        this.type = Objects.requireNonNull(type, "type");
        this.placementNumerote = Objects.requireNonNull(placementNumerote, "placementNumerote");
    }
    public Integer getIdSpectacle() { return idSpectacle; }
    public String getTitre() { return titre; }
    public TypeSpectacle getType() { return type; }
    public Boolean getPlacementNumerote() { return placementNumerote; }
    public void setPlacementNumerote(Boolean placementNumerote) {
        this.placementNumerote = Objects.requireNonNull(placementNumerote, "placementNumerote");
    }
}

package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/** Prix d'une catégorie pour un événement.
 * COUCHE : Entité · IDENTITÉ : (événement, categorie) · NE FAIT PAS : le calcul de remboursement. */
@Entity
@Table(name = "tarif")
public class Tarif {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarif")
    private Integer idTarif;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_evenement", nullable = false)
    private Evenement evenement;
    @Column(name = "categorie", nullable = false, length = 10)
    private String categorie;
    @Column(name = "prix", nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;
    protected Tarif() {}
    public Tarif(Evenement evenement, String categorie, BigDecimal prix) {
        this.evenement = Objects.requireNonNull(evenement, "evenement");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
        this.prix = Objects.requireNonNull(prix, "prix");
    }
    public Integer getIdTarif() { return idTarif; }
    public Evenement getEvenement() { return evenement; }
    public String getCategorie() { return categorie; }
    public BigDecimal getPrix() { return prix; }
}

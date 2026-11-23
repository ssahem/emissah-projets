package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Place physique dans une salle.
 * COUCHE : Entité · IDENTITÉ : (salle, rang, numero) · NE FAIT PAS : les règles métier. */
@Entity
@Table(name = "place")
public class Place {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_place")
    private Integer idPlace;
    @Column(name = "rang", nullable = false, length = 5)
    private String rang;
    @Column(name = "numero", nullable = false)
    private Integer numero;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_salle", nullable = false)
    private Salle salle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;
    protected Place() {}
    public Place(String rang, Integer numero, Salle salle, Categorie categorie) {
        this.rang = Objects.requireNonNull(rang, "rang");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.salle = Objects.requireNonNull(salle, "salle");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
    }
    public Integer getIdPlace() { return idPlace; }
    public String getRang() { return rang; }
    public Integer getNumero() { return numero; }
    public Salle getSalle() { return salle; }
    public Categorie getCategorie() { return categorie; }

    /** Applique une modification complète de la place (F2). */
    public void modifier(String rang, Integer numero, Salle salle, Categorie categorie) {
        this.rang = Objects.requireNonNull(rang, "rang");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.salle = Objects.requireNonNull(salle, "salle");
        this.categorie = Objects.requireNonNull(categorie, "categorie");
    }
}

package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Statut d'une réservation (PROVISOIRE, PAYEE, ANNULEE).
 * COUCHE : Entité · IDENTITÉ : libelle · NE FAIT PAS : la machine à états. */
@Entity
@Table(name = "statut")
public class Statut {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Integer idStatut;
    @Column(name = "libelle", nullable = false, length = 30)
    private String libelle;
    protected Statut() {}
    public Statut(String libelle) { this.libelle = Objects.requireNonNull(libelle, "libelle"); }
    public Integer getIdStatut() { return idStatut; }
    public String getLibelle() { return libelle; }
}

package com.billetterie.billetterie_api.entity;

import jakarta.persistence.*;
import java.util.Objects;

/** Client identifié par son email.
 * COUCHE : Entité · IDENTITÉ : email · NE FAIT PAS : la validation du format. */
@Entity
@Table(name = "client")
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Integer idClient;
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    protected Client() {}
    public Client(String nom, String prenom, String email) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
    }
    public Integer getIdClient() { return idClient; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public void modifier(String nom, String prenom, String email) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
    }
}

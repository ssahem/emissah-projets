package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Client d'une intervention (F4). Adresse et commune sont des textes simples (S-10, S-11).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idClient.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "client")
public class Client {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Integer idClient;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Column(name = "commune", nullable = false, length = 100)
    private String commune;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Client() {
    }

    public Client(String nom, String prenom, String email, String telephone,
                  String adresse, String commune) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = Objects.requireNonNull(telephone, "telephone");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.commune = Objects.requireNonNull(commune, "commune");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdClient() { return idClient; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getAdresse() { return adresse; }
    public String getCommune() { return commune; }

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(String nom, String prenom, String email, String telephone,
                         String adresse, String commune) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.prenom = Objects.requireNonNull(prenom, "prenom");
        this.email = Objects.requireNonNull(email, "email");
        this.telephone = Objects.requireNonNull(telephone, "telephone");
        this.adresse = Objects.requireNonNull(adresse, "adresse");
        this.commune = Objects.requireNonNull(commune, "commune");
    }
}

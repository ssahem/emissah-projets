package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Journal des réouvertures de trimestre (F15, arbitrage A-04).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : conserver date, motif et auteur de chaque réouverture.
 * IDENTITÉ     : idHistorique ; une seule réouverture par trimestre (UNIQUE).
 * UTILISÉE PAR : HistoriqueReouvertureRepository, ReglesMetierService.
 * NE FAIT PAS  : de logique métier ni d'authentification (auteur = texte libre).
 */
@Entity
@Table(name = "historique_reouverture_trimestre")
public class HistoriqueReouverture {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;

    @Column(name = "id_trimestre", nullable = false, unique = true)
    private Integer idTrimestre;

    @Column(name = "date_reouverture", nullable = false)
    private LocalDate dateReouverture;

    @Column(name = "motif", nullable = false, columnDefinition = "text")
    private String motif;

    @Column(name = "auteur", nullable = false)
    private String auteur;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected HistoriqueReouverture() {
    }

    /** Crée une trace de réouverture ; le motif et l'auteur sont fournis par l'appelant. */
    public HistoriqueReouverture(Integer idTrimestre, LocalDate dateReouverture,
                                 String motif, String auteur) {
        this.idTrimestre = Objects.requireNonNull(idTrimestre, "idTrimestre");
        this.dateReouverture = Objects.requireNonNull(dateReouverture, "dateReouverture");
        this.motif = Objects.requireNonNull(motif, "motif");
        this.auteur = Objects.requireNonNull(auteur, "auteur");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdHistorique() { return idHistorique; }
    public Integer getIdTrimestre() { return idTrimestre; }
    public LocalDate getDateReouverture() { return dateReouverture; }
    public String getMotif() { return motif; }
    public String getAuteur() { return auteur; }
}

package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Inscription d'un élève dans une classe (F2, F6, F11).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : conserve l'historique des affectations ; date_fin null = inscription
 *                courante.
 * IDENTITÉ     : numeroInscription (clé métier utilisée par note.numero_inscription).
 * UTILISÉE PAR : InscriptionRepository, les services de consultation et de restitution.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "inscription")
public class Inscription {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscription")
    private Integer idInscription;

    @Column(name = "date_inscription", nullable = false)
    private LocalDate dateInscription;

    @Column(name = "id_eleve", nullable = false)
    private Integer idEleve;

    @Column(name = "id_classe", nullable = false)
    private Integer idClasse;

    @Column(name = "numero_inscription", nullable = false, unique = true)
    private String numeroInscription;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    /** Relation en lecture seule pour résoudre le nom de l'élève depuis un numéro d'inscription. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_eleve", insertable = false, updatable = false)
    private Eleve eleve;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Inscription() {
    }

    /** Crée une inscription courante à la date donnée. */
    public Inscription(LocalDate dateInscription, Integer idEleve, Integer idClasse,
                       String numeroInscription) {
        this.dateInscription = Objects.requireNonNull(dateInscription, "dateInscription");
        this.idEleve = Objects.requireNonNull(idEleve, "idEleve");
        this.idClasse = Objects.requireNonNull(idClasse, "idClasse");
        this.numeroInscription = Objects.requireNonNull(numeroInscription, "numeroInscription");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdInscription() { return idInscription; }
    public LocalDate getDateInscription() { return dateInscription; }
    public Integer getIdEleve() { return idEleve; }
    public Integer getIdClasse() { return idClasse; }
    public String getNumeroInscription() { return numeroInscription; }
    public LocalDate getDateFin() { return dateFin; }

    /** Retourne l'élève rattaché (lecture seule, A-01). */
    public Eleve getEleve() { return eleve; }

    /** Clôture l'affectation courante sans effacer les notes rattachées au numéro. */
    public void clore(LocalDate dateFin) { this.dateFin = Objects.requireNonNull(dateFin, "dateFin"); }
}

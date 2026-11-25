package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entretien ou immobilisation d'un véhicule (F5).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code entretien}.
 * IDENTITÉ     : le couple (véhicule, date de début), la date métier étant date_debut (D-02).
 * UTILISÉ PAR  : EntretienRepository, EntretienService, VueRepository.
 * NE FAIT PAS  : de règle de coût ni de sérialisation HTTP.
 */
@Entity
@Table(name = "entretien")
public class Entretien {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entretien")
    private Integer idEntretien;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "cout", nullable = false)
    private BigDecimal cout;

    @Column(name = "km_compteur", nullable = false)
    private Integer kmCompteur;

    @Column(name = "id_vehicule", nullable = false)
    private Integer idVehicule;

    @Column(name = "id_type_entretien", nullable = false)
    private Integer idTypeEntretien;

    @Column(name = "id_prestataire", nullable = false)
    private Integer idPrestataire;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Entretien() {
    }

    /** Crée un entretien ; D-02 mappe « la date » de l'énoncé sur date_debut. */
    public Entretien(LocalDate dateDebut, LocalDate dateFin, BigDecimal cout, Integer kmCompteur,
                     Integer idVehicule, Integer idTypeEntretien, Integer idPrestataire) {
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
        this.dateFin = dateFin;
        this.cout = Objects.requireNonNull(cout, "cout");
        this.kmCompteur = Objects.requireNonNull(kmCompteur, "kmCompteur");
        this.idVehicule = Objects.requireNonNull(idVehicule, "idVehicule");
        this.idTypeEntretien = Objects.requireNonNull(idTypeEntretien, "idTypeEntretien");
        this.idPrestataire = Objects.requireNonNull(idPrestataire, "idPrestataire");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdEntretien() { return idEntretien; }

    /** Retourne la date de début de l'immobilisation. */
    public LocalDate getDateDebut() { return dateDebut; }

    /** Retourne la date de fin de l'immobilisation. */
    public LocalDate getDateFin() { return dateFin; }

    /** Retourne le coût. */
    public BigDecimal getCout() { return cout; }

    /** Retourne le kilométrage relevé. */
    public Integer getKmCompteur() { return kmCompteur; }

    /** Retourne le véhicule concerné. */
    public Integer getIdVehicule() { return idVehicule; }

    /** Retourne le type d'entretien. */
    public Integer getIdTypeEntretien() { return idTypeEntretien; }

    /** Retourne le prestataire. */
    public Integer getIdPrestataire() { return idPrestataire; }

    // ═════════════════════════════════════════════════════════════════════════
    //  COMPORTEMENT
    // ═════════════════════════════════════════════════════════════════════════

    /** Modifie le coût et le prestataire ; la date et le kilométrage sont figés (F5). */

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(BigDecimal cout, Integer idPrestataire) {
        this.cout = Objects.requireNonNull(cout, "cout");
        this.idPrestataire = Objects.requireNonNull(idPrestataire, "idPrestataire");
    }

    /** Retourne la fin effective de l'immobilisation (date_debut si date_fin absente). */

    // ── finEffective() ───────────────────────────────────────────────────────

    /**
     * Exécute finEffective().
     */

    public LocalDate finEffective() {
        return dateFin == null ? dateDebut : dateFin;
    }
}

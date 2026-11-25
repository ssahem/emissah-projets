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
 * Véhicule du parc (F2).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code vehicule}.
 * IDENTITÉ     : l'immatriculation, unique dans le schéma.
 * UTILISÉ PAR  : VehiculeRepository, VehiculeService, AffectationService.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP.
 */
@Entity
@Table(name = "vehicule")
public class Vehicule {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    private Integer idVehicule;

    @Column(name = "immatriculation", nullable = false, unique = true)
    private String immatriculation;

    @Column(name = "marque", nullable = false)
    private String marque;

    @Column(name = "modele", nullable = false)
    private String modele;

    @Column(name = "date_mise_en_service", nullable = false)
    private LocalDate dateMiseEnService;

    @Column(name = "mode_acquisition", nullable = false)
    private String modeAcquisition;

    @Column(name = "partage", nullable = false)
    private Boolean partage;

    @Column(name = "prix_achat")
    private BigDecimal prixAchat;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Column(name = "id_site", nullable = false)
    private Integer idSite;

    @Column(name = "id_vehicule_remplace", unique = true)
    private Integer idVehiculeRemplace;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Vehicule() {
    }

    /** Crée un véhicule ; le mode d'acquisition par défaut est PROPRIETE. */
    public Vehicule(String immatriculation, String marque, String modele,
                    LocalDate dateMiseEnService, Integer idSite,
                    String modeAcquisition, Boolean partage, BigDecimal prixAchat) {
        this.immatriculation = Objects.requireNonNull(immatriculation, "immatriculation");
        this.marque = Objects.requireNonNull(marque, "marque");
        this.modele = Objects.requireNonNull(modele, "modele");
        this.dateMiseEnService = Objects.requireNonNull(dateMiseEnService, "dateMiseEnService");
        this.idSite = Objects.requireNonNull(idSite, "idSite");
        this.modeAcquisition = modeAcquisition == null ? "PROPRIETE" : modeAcquisition;
        this.partage = partage != null && partage;
        this.prixAchat = prixAchat;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'identifiant technique. */
    public Integer getIdVehicule() { return idVehicule; }

    /** Retourne l'immatriculation. */
    public String getImmatriculation() { return immatriculation; }

    /** Retourne la marque. */
    public String getMarque() { return marque; }

    /** Retourne le modèle. */
    public String getModele() { return modele; }

    /** Retourne la date de mise en service. */
    public LocalDate getDateMiseEnService() { return dateMiseEnService; }

    /** Retourne le mode d'acquisition. */
    public String getModeAcquisition() { return modeAcquisition; }

    /** Indique si le véhicule est partagé. */
    public Boolean getPartage() { return partage; }

    /** Retourne le prix d'achat éventuel. */
    public BigDecimal getPrixAchat() { return prixAchat; }

    /** Retourne la date de sortie éventuelle. */
    public LocalDate getDateSortie() { return dateSortie; }

    /** Retourne le site courant (décision A-01). */
    public Integer getIdSite() { return idSite; }

    /** Retourne l'identifiant du véhicule remplacé. */
    public Integer getIdVehiculeRemplace() { return idVehiculeRemplace; }

    // ═════════════════════════════════════════════════════════════════════════
    //  COMPORTEMENT
    // ═════════════════════════════════════════════════════════════════════════

    /** Modifie le site lors d'un transfert ; immatriculation et date sont figées (F2). */

    // ── modifier() ───────────────────────────────────────────────────────────

    /**
     * Exécute modifier().
     */

    public void modifier(Integer idSite, String marque, String modele) {
        this.idSite = Objects.requireNonNull(idSite, "idSite");
        this.marque = Objects.requireNonNull(marque, "marque");
        this.modele = Objects.requireNonNull(modele, "modele");
    }
}

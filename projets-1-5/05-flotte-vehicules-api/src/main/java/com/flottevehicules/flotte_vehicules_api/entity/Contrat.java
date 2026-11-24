package com.flottevehicules.flotte_vehicules_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrat de location ou de leasing d'un véhicule.
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table {@code contrat}, lue par les vues de coût.
 * IDENTITÉ     : le véhicule, unique dans le schéma.
 * UTILISÉ PAR  : ContratRepository, vues SQL.
 * NE FAIT PAS  : de calcul de loyer.
 */
@Entity
@Table(name = "contrat")
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer idContrat;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;

    @Column(name = "loyer_mensuel", nullable = false)
    private BigDecimal loyerMensuel;

    @Column(name = "km_autorise", nullable = false)
    private Integer kmAutorise;

    @Column(name = "id_vehicule", nullable = false, unique = true)
    private Integer idVehicule;

    /** Constructeur vide exigé par JPA. */
    protected Contrat() {
    }

    /** Retourne l'identifiant technique. */
    public Integer getIdContrat() { return idContrat; }

    /** Retourne la date de début. */
    public LocalDate getDateDebut() { return dateDebut; }

    /** Retourne la durée en mois. */
    public Integer getDureeMois() { return dureeMois; }

    /** Retourne le loyer mensuel. */
    public BigDecimal getLoyerMensuel() { return loyerMensuel; }

    /** Retourne le kilométrage autorisé. */
    public Integer getKmAutorise() { return kmAutorise; }

    /** Retourne le véhicule. */
    public Integer getIdVehicule() { return idVehicule; }
}

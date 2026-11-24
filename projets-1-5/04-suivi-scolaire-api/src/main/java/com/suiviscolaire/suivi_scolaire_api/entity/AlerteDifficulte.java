package com.suiviscolaire.suivi_scolaire_api.entity;

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
 * Alerte de difficulté produite par la tâche planifiée (F16, arbitrage A-05).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : mémorise moyenne et écart par inscription et trimestre pour éviter les
 *                doublons.
 * IDENTITÉ     : (idInscription, numeroTrimestre) — unicité en base.
 * UTILISÉE PAR : AlerteDifficulteRepository, AlertesDifficulteService.
 * NE FAIT PAS  : de logique métier ni de calcul (réalisé en service).
 */
@Entity
@Table(name = "alerte_difficulte")
public class AlerteDifficulte {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Integer idAlerte;

    @Column(name = "id_inscription", nullable = false)
    private Integer idInscription;

    @Column(name = "numero_trimestre", nullable = false)
    private Integer numeroTrimestre;

    @Column(name = "moyenne", nullable = false)
    private BigDecimal moyenne;

    @Column(name = "ecart", nullable = false)
    private BigDecimal ecart;

    @Column(name = "date_alerte", nullable = false)
    private LocalDate dateAlerte;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected AlerteDifficulte() {
    }

    /** Crée une alerte avec la moyenne, l'écart et la date du jour. */
    public AlerteDifficulte(Integer idInscription, Integer numeroTrimestre,
                            BigDecimal moyenne, BigDecimal ecart, LocalDate dateAlerte) {
        this.idInscription = Objects.requireNonNull(idInscription, "idInscription");
        this.numeroTrimestre = Objects.requireNonNull(numeroTrimestre, "numeroTrimestre");
        this.moyenne = Objects.requireNonNull(moyenne, "moyenne");
        this.ecart = Objects.requireNonNull(ecart, "ecart");
        this.dateAlerte = Objects.requireNonNull(dateAlerte, "dateAlerte");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdAlerte() { return idAlerte; }
    public Integer getIdInscription() { return idInscription; }
    public Integer getNumeroTrimestre() { return numeroTrimestre; }
    public BigDecimal getMoyenne() { return moyenne; }
    public BigDecimal getEcart() { return ecart; }
    public LocalDate getDateAlerte() { return dateAlerte; }
}

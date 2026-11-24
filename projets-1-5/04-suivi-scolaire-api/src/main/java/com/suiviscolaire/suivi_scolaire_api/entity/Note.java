package com.suiviscolaire.suivi_scolaire_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Note attribuée à une inscription pour un enseignement (F5).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter une ligne de la table note ; l'élève est résolu par
 *                numero_inscription.
 * IDENTITÉ     : idNote (technique) ; le couple (numeroInscription, enseignement, trimestre,
 *                type) caractérise la mesure.
 * UTILISÉE PAR : NoteRepository, NoteService, ConsultationService.
 * NE FAIT PAS  : de logique métier.
 */
@Entity
@Table(name = "note")
public class Note {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Integer idNote;

    @Column(name = "valeur", nullable = false)
    private BigDecimal valeur;

    @Column(name = "numero_trimestre", nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Integer numeroTrimestre;

    @Column(name = "id_type_eval", nullable = false)
    private Integer idTypeEval;

    @Column(name = "id_enseignement", nullable = false)
    private Integer idEnseignement;

    @Column(name = "numero_inscription", nullable = false)
    private String numeroInscription;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    /** Constructeur vide exigé par JPA. */
    protected Note() {
    }

    /** Crée une note ; la valeur et le trimestre sont validés par le service (A-01, A-03). */
    public Note(BigDecimal valeur, Integer numeroTrimestre, Integer idTypeEval,
                Integer idEnseignement, String numeroInscription) {
        this.valeur = Objects.requireNonNull(valeur, "valeur");
        this.numeroTrimestre = Objects.requireNonNull(numeroTrimestre, "numeroTrimestre");
        this.idTypeEval = Objects.requireNonNull(idTypeEval, "idTypeEval");
        this.idEnseignement = Objects.requireNonNull(idEnseignement, "idEnseignement");
        this.numeroInscription = Objects.requireNonNull(numeroInscription, "numeroInscription");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdNote() { return idNote; }
    public BigDecimal getValeur() { return valeur; }
    public Integer getNumeroTrimestre() { return numeroTrimestre; }
    public Integer getIdTypeEval() { return idTypeEval; }
    public Integer getIdEnseignement() { return idEnseignement; }
    public String getNumeroInscription() { return numeroInscription; }

    /** Modifie la valeur ; l'élève, la matière et le trimestre restent figés (F5). */
    public void modifierValeur(BigDecimal valeur) { this.valeur = Objects.requireNonNull(valeur, "valeur"); }
}

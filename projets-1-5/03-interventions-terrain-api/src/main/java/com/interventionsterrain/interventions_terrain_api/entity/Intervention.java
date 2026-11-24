package com.interventionsterrain.interventions_terrain_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Intervention planifiée (F5). Le statut est porté par l'affectation responsable (A-03).
 *
 * COUCHE       : Entité (modèle de données)
 * RÔLE         : représenter la ligne de table, ses colonnes et ses relations JPA, sans
 *                logique métier.
 * IDENTITÉ     : idIntervention.
 * DÉPEND DE    : aucune classe applicative.
 * UTILISÉ PAR  : repositories, mappers et services.
 * NE FAIT PAS  : de logique métier ni de sérialisation HTTP directe.
 */
@Entity
@Table(name = "intervention")
public class Intervention {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intervention")
    private Integer idIntervention;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "debut_prevu", nullable = false)
    private LocalDateTime debutPrevu;

    @Column(name = "fin_prevue", nullable = false)
    private LocalDateTime finPrevue;

    @Column(name = "note_client")
    private Short noteClient;

    @Column(name = "commentaire_client")
    private String commentaireClient;

    @Column(name = "id_client", nullable = false)
    private Integer idClient;

    @Column(name = "id_adresse", nullable = false)
    private Integer idAdresse;

    @Column(name = "id_intervention_origine")
    private Integer idInterventionOrigine;

    @Column(name = "code_type_intervention", nullable = false)
    private String codeTypeIntervention;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ═════════════════════════════════════════════════════════════════════════

    protected Intervention() {
    }

    public Intervention(LocalDateTime dateDemande, LocalDateTime debutPrevu, LocalDateTime finPrevue,
                        Integer idClient, Integer idAdresse, String codeTypeIntervention) {
        this.dateDemande = Objects.requireNonNull(dateDemande, "dateDemande");
        this.debutPrevu = Objects.requireNonNull(debutPrevu, "debutPrevu");
        this.finPrevue = Objects.requireNonNull(finPrevue, "finPrevue");
        this.idClient = Objects.requireNonNull(idClient, "idClient");
        this.idAdresse = Objects.requireNonNull(idAdresse, "idAdresse");
        this.codeTypeIntervention = Objects.requireNonNull(codeTypeIntervention, "codeTypeIntervention");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACCESSEURS
    // ═════════════════════════════════════════════════════════════════════════

    public Integer getIdIntervention() { return idIntervention; }
    public LocalDateTime getDateDemande() { return dateDemande; }
    public LocalDateTime getDebutPrevu() { return debutPrevu; }
    public LocalDateTime getFinPrevue() { return finPrevue; }
    public Short getNoteClient() { return noteClient; }
    public String getCommentaireClient() { return commentaireClient; }
    public Integer getIdClient() { return idClient; }
    public Integer getIdAdresse() { return idAdresse; }
    public Integer getIdInterventionOrigine() { return idInterventionOrigine; }
    public String getCodeTypeIntervention() { return codeTypeIntervention; }

    public void modifierCreneau(LocalDateTime debutPrevu, LocalDateTime finPrevue) {
        this.debutPrevu = Objects.requireNonNull(debutPrevu, "debutPrevu");
        this.finPrevue = Objects.requireNonNull(finPrevue, "finPrevue");
    }

    public void modifierOrigine(Integer idInterventionOrigine) {
        this.idInterventionOrigine = idInterventionOrigine;
    }
}

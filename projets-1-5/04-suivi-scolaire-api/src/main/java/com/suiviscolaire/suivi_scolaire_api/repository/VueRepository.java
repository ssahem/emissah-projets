package com.suiviscolaire.suivi_scolaire_api.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Lecture des vues SQL en lecture seule (F16 à F19).
 *
 * COUCHE       : Repository (persistance, JdbcTemplate)
 * RÔLE         : interroger les vues de moyennes, de rang, de bulletin et d'absences.
 * DÉPEND DE    : JdbcTemplate ; les vues ne sont JAMAIS mappées comme entités JPA.
 * UTILISÉ PAR  : AlertesDifficulteService, RestitutionService.
 * NE FAIT PAS  : d'écriture, de calcul métier ni de conversion en DTO (rôle du service).
 */
@Repository
public class VueRepository {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final JdbcTemplate jdbcTemplate;

    /** Spring fournit le JdbcTemplate configuré sur la même source de données. */
    public VueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES SUR LES VUES
    // ═════════════════════════════════════════════════════════════════════════

    /** Moyennes par matière d'une inscription pour un trimestre (F17). */
    public List<Map<String, Object>> moyennesMatiere(Integer idInscription, Integer trimestre) {
        return jdbcTemplate.queryForList(
                "SELECT id_inscription, numero_trimestre, id_enseignement, id_matiere, moyenne_matiere "
                        + "FROM v_moyenne_matiere WHERE id_inscription = ? AND numero_trimestre = ? "
                        + "ORDER BY id_matiere",
                idInscription, trimestre);
    }

    /** Moyennes générales d'une inscription (F16, F18). */
    public List<Map<String, Object>> moyennesGenerales(Integer idInscription) {
        return jdbcTemplate.queryForList(
                "SELECT id_inscription, numero_trimestre, moyenne_generale "
                        + "FROM v_moyenne_generale WHERE id_inscription = ? ORDER BY numero_trimestre",
                idInscription);
    }


    // ── moyenneGenerale() ────────────────────────────────────────────────────

    /**
     * Moyenne générale d'une inscription pour un trimestre (F18).
     */

    public BigDecimal moyenneGenerale(Integer idInscription, Integer trimestre) {
        return jdbcTemplate.query(
                "SELECT moyenne_generale FROM v_moyenne_generale "
                        + "WHERE id_inscription = ? AND numero_trimestre = ?",
                rs -> rs.next() ? rs.getBigDecimal(1) : null, idInscription, trimestre);
    }

    /** Rangs d'une classe pour un trimestre (F19). */
    public List<Map<String, Object>> rangsClasse(Integer idClasse, Integer trimestre) {
        return jdbcTemplate.queryForList(
                "SELECT id_inscription, numero_trimestre, id_classe, moyenne_generale, rang "
                        + "FROM v_rang_classe WHERE id_classe = ? AND numero_trimestre = ? "
                        + "ORDER BY moyenne_generale DESC NULLS LAST",
                idClasse, trimestre);
    }

    /** Lignes de bulletin d'un bulletin (F19). */
    public List<Map<String, Object>> lignesBulletin(Integer idBulletin) {
        return jdbcTemplate.queryForList(
                "SELECT id_ligne, id_bulletin, id_enseignement, id_matiere, id_professeur, "
                        + "moyenne_matiere, appreciation FROM v_ligne_bulletin "
                        + "WHERE id_bulletin = ? ORDER BY id_matiere", idBulletin);
    }

    /** Enseignements sans coefficient dans une classe (contrôle de complétude). */
    public List<Map<String, Object>> coefficientsManquants(Integer idClasse) {
        return jdbcTemplate.queryForList(
                "SELECT id_enseignement, id_classe, id_matiere, id_niveau "
                        + "FROM v_coefficient_manquant WHERE id_classe = ?", idClasse);
    }

    /** Absences à signaler pour une inscription (vue hors énoncé, conservée pour information). */
    public List<Map<String, Object>> absencesASignaler(Integer idInscription) {
        return jdbcTemplate.queryForList(
                "SELECT id_inscription, id_eleve, nb_absences_non_justifiees, seuil_alerte_absences "
                        + "FROM v_absences_a_signaler WHERE id_inscription = ?", idInscription);
    }
}

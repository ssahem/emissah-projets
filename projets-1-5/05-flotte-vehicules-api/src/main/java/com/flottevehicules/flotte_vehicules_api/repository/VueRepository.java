package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Lecture en lecture seule des dix vues SQL (F12, F15, F16, F17, F18).
 *
 * COUCHE       : Repository (accès SQL dédié)
 * RÔLE         : interroger les vues via JdbcTemplate ; aucune vue n'est mappée comme entité
 *                JPA.
 * DÉPEND DE    : JdbcTemplate uniquement.
 * UTILISÉ PAR  : VehiculeService, AlerteService, EntretienService.
 * NE FAIT PAS  : d'écriture, de règle métier ni de conversion en DTO d'API.
 */
@Repository
public class VueRepository {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Liste fermée des vues autorisées : aucune interpolation SQL arbitraire. */
    private static final Set<String> VUES = Set.of(
            "v_bilan_vehicule", "v_conflit_affectation_entretien", "v_cout_total_vehicule",
            "v_depense_conducteur", "v_depense_sans_conducteur", "v_echeance_entretien",
            "v_remplacement_incoherent", "v_trajet_hors_affectation", "v_trajet_incoherent",
            "v_vehicule_sans_contrat");

    private final JdbcTemplate jdbcTemplate;

    /** Ligne de la vue v_echeance_entretien. */

    // ── EcheanceRow() ────────────────────────────────────────────────────────

    /**
     * Exécute EcheanceRow().
     */

    public record EcheanceRow(Integer idVehicule, Integer idTypeEntretien, String libelle,
                              LocalDate dateDernier, Integer kmDernier, Integer kmProchain,
                              LocalDate dateProchaine, Integer kmActuel, boolean enRetard) {
    }

    /** Ligne de la vue v_bilan_vehicule. */

    // ── BilanRow() ───────────────────────────────────────────────────────────

    /**
     * Exécute BilanRow().
     */

    public record BilanRow(Integer idVehicule, String immatriculation, BigDecimal coutTotal,
                           Integer ageAnnees, Integer kmCompteurActuel, BigDecimal coutAuKm) {
    }

    /** Ligne de la vue v_cout_total_vehicule. */

    // ── CoutRow() ────────────────────────────────────────────────────────────

    /**
     * Exécute CoutRow().
     */

    public record CoutRow(Integer idVehicule, String immatriculation, BigDecimal coutAchat,
                          BigDecimal coutContrats, BigDecimal coutEntretiens,
                          BigDecimal coutDepenses, BigDecimal coutTotal) {
    }

    /** Dernier kilométrage connu d'un véhicule et date de sa relevée (F14). */

    // ── DernierKm() ──────────────────────────────────────────────────────────

    /**
     * Exécute DernierKm().
     */

    public record DernierKm(Integer km, LocalDate date) {
    }

    /** Relevé de restitution agrégé par véhicule sur un mois (F19). */

    // ── KilometrageRow() ─────────────────────────────────────────────────────

    /**
     * Exécute KilometrageRow().
     */

    public record KilometrageRow(Integer idVehicule, String immatriculation, Integer km) {
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit le JdbcTemplate. */
    public VueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    // ── echeancesVehicule() ──────────────────────────────────────────────────

    /**
     * Retourne les échéances périodiques d'un véhicule, triées par date croissante.
     *
     * @param idVehicule identifiant du véhicule
     * @return une liste vide si aucune catégorie n'est PERIODIQUE
     */
    public List<EcheanceRow> echeancesVehicule(Integer idVehicule) {
        return jdbcTemplate.query(
                "SELECT * FROM v_echeance_entretien WHERE id_vehicule = ? "
                        + "ORDER BY date_prochaine ASC, id_type_entretien ASC",
                VueRepository::mapEcheance, idVehicule);
    }

    // ── toutesEcheances() ────────────────────────────────────────────────────

    /** Retourne toutes les échéances périodiques, utilisées par la tâche F16. */
    public List<EcheanceRow> toutesEcheances() {
        return jdbcTemplate.query(
                "SELECT * FROM v_echeance_entretien ORDER BY id_vehicule, id_type_entretien",
                VueRepository::mapEcheance);
    }

    // ── bilanVehicule() ──────────────────────────────────────────────────────

    /**
     * Lit le bilan d'un véhicule (coût des entretiens et compteur courant).
     *
     * @return le bilan, ou {@link Optional#empty()} si le véhicule est inconnu
     */
    public Optional<BilanRow> bilanVehicule(Integer idVehicule) {
        return jdbcTemplate.query(
                "SELECT * FROM v_bilan_vehicule WHERE id_vehicule = ?",
                rs -> rs.next() ? Optional.of(mapBilan(rs)) : Optional.empty(), idVehicule);
    }

    // ── coutEntretiens() ─────────────────────────────────────────────────────

    /**
     * Retourne le coût total des entretiens d'un véhicule (D-03).
     *
     * @return zéro si le véhicule n'a aucun entretien
     */
    public BigDecimal coutEntretiens(Integer idVehicule) {
        BigDecimal cout = jdbcTemplate.queryForObject(
                "SELECT cout_entretiens FROM v_cout_total_vehicule WHERE id_vehicule = ?",
                BigDecimal.class, idVehicule);
        return cout == null ? BigDecimal.ZERO : cout;
    }

    // ── coutsVehicule() ──────────────────────────────────────────────────────

    /** Retourne la ventilation complète des coûts d'un véhicule. */
    public Optional<CoutRow> coutsVehicule(Integer idVehicule) {
        return jdbcTemplate.query(
                "SELECT * FROM v_cout_total_vehicule WHERE id_vehicule = ?",
                rs -> rs.next() ? Optional.of(mapCout(rs)) : Optional.empty(), idVehicule);
    }

    // ── compteConflits() ─────────────────────────────────────────────────────

    /** Compte les immobilisations recouvrant une affectation dans la vue dédiée (F12). */
    public int compteConflits(Integer idAffectation) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM v_conflit_affectation_entretien WHERE id_affectation = ?",
                Integer.class, idAffectation);
        return count == null ? 0 : count;
    }

    // ── dernierKm() ──────────────────────────────────────────────────────────

    /**
     * Retourne le plus grand kilométrage connu d'un véhicule et la date de la relevée (F14).
     *
 * Les trois sources sont les restitutions, les entretiens et les trajets. Le compteur
 *   courant
     * est toujours la valeur maximale, conformément à la décision D-01.
     *
     * @return un optional vide si aucune relevée n'existe
     */
    public Optional<DernierKm> dernierKm(Integer idVehicule) {
        String sql = "SELECT km, date FROM ("
                + " SELECT a.km_restitution AS km, a.date_fin AS date FROM affectation a"
                + " WHERE a.id_vehicule = ? AND a.km_restitution IS NOT NULL"
                + " UNION ALL"
                + " SELECT e.km_compteur AS km, e.date_debut AS date FROM entretien e"
                + " WHERE e.id_vehicule = ?"
                + " UNION ALL"
                + " SELECT t.km_arrivee AS km, t.date_trajet AS date FROM trajet t"
                + " JOIN affectation a ON a.id_affectation = t.id_affectation"
                + " WHERE a.id_vehicule = ?"
                + ") releves ORDER BY km DESC LIMIT 1";
        return jdbcTemplate.query(sql,
                rs -> rs.next()
                        ? Optional.of(new DernierKm(rs.getInt("km"), toLocalDate(rs, "date")))
                        : Optional.empty(),
                idVehicule, idVehicule, idVehicule);
    }

    // ── kilometragesParSite() ────────────────────────────────────────────────

    /**
 * Retourne les relevés de restitution d'un site sur un intervalle, agrégés par véhicule
 *   (F19).
     *
     * Limite A-01 : le site est le site courant de vehicule.id_site, pas le site d'origine
 * au moment de l'affectation ; un transfert en cours de mois est donc compté sur le site
 *   courant.
     */
    public List<KilometrageRow> kilometragesParSite(Integer idSite, LocalDate debut, LocalDate fin) {
        return jdbcTemplate.query(
                "SELECT a.id_vehicule, v.immatriculation, SUM(a.km_restitution) AS km"
                        + " FROM affectation a JOIN vehicule v ON v.id_vehicule = a.id_vehicule"
                        + " WHERE v.id_site = ? AND a.date_fin BETWEEN ? AND ?"
                        + " AND a.km_restitution IS NOT NULL"
                        + " GROUP BY a.id_vehicule, v.immatriculation"
                        + " ORDER BY a.id_vehicule",
                (rs, rowNum) -> new KilometrageRow(
                        rs.getInt("id_vehicule"), rs.getString("immatriculation"), rs.getInt("km")),
                idSite, Date.valueOf(debut), Date.valueOf(fin));
    }

    // ── lireVue() ────────────────────────────────────────────────────────────

    /**
     * Lit une vue autorisée et retourne ses lignes sous forme de maps.
     *
     * @param nomVue nom exact d'une des dix vues du schéma
     * @throws DonneeInvalideException si le nom n'est pas dans la liste fermée
     */
    public List<Map<String, Object>> lireVue(String nomVue) {
        if (!VUES.contains(nomVue)) {
            throw new DonneeInvalideException("vue-inconnue", "Vue SQL non autorisée : " + nomVue);
        }
        return jdbcTemplate.queryForList("SELECT * FROM " + nomVue);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── mapEcheance() ────────────────────────────────────────────────────────

    /**
     * Exécute mapEcheance().
     */

    private static EcheanceRow mapEcheance(ResultSet rs, int rowNum) throws SQLException {
        return new EcheanceRow(
                rs.getInt("id_vehicule"),
                rs.getInt("id_type_entretien"),
                rs.getString("libelle"),
                toLocalDate(rs, "date_dernier"),
                (Integer) rs.getObject("km_dernier"),
                (Integer) rs.getObject("km_prochain"),
                toLocalDate(rs, "date_prochaine"),
                (Integer) rs.getObject("km_actuel"),
                rs.getBoolean("en_retard"));
    }

    // ── mapBilan() ───────────────────────────────────────────────────────────

    /**
     * Exécute mapBilan().
     */

    private static BilanRow mapBilan(ResultSet rs) throws SQLException {
        return new BilanRow(
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getBigDecimal("cout_total"),
                (Integer) rs.getObject("age_annees"),
                (Integer) rs.getObject("km_compteur_actuel"),
                rs.getBigDecimal("cout_au_km"));
    }

    // ── mapCout() ────────────────────────────────────────────────────────────

    /**
     * Exécute mapCout().
     */

    private static CoutRow mapCout(ResultSet rs) throws SQLException {
        return new CoutRow(
                rs.getInt("id_vehicule"),
                rs.getString("immatriculation"),
                rs.getBigDecimal("cout_achat"),
                rs.getBigDecimal("cout_contrats"),
                rs.getBigDecimal("cout_entretiens"),
                rs.getBigDecimal("cout_depenses"),
                rs.getBigDecimal("cout_total"));
    }

    // ── toLocalDate() ────────────────────────────────────────────────────────

    /**
     * Exécute toLocalDate().
     */

    private static LocalDate toLocalDate(ResultSet rs, String colonne) throws SQLException {
        Date date = rs.getDate(colonne);
        return date == null ? null : date.toLocalDate();
    }
}

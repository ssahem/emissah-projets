package com.suiviscolaire.suivi_scolaire_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.suiviscolaire.suivi_scolaire_api.service.AlertesDifficulteService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parcours d'intégration complet sur PostgreSQL (F1 à F19).
 *
 * COUCHE       : Service
 * RÔLE         : Parcours d'intégration complet sur PostgreSQL (F1 à F19).
 * TYPE         : Intégration (Testcontainers PostgreSQL 16)
 * STRATÉGIE    : migrations Flyway V1 à V4, horloge fixe au 24 novembre 2026, jeu de données minimal
 *                recréé avant chaque test, appels HTTP réels via MockMvc.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SuiviScolaireIntegrationTest {

    private static final String MOTIF = "Correction d'une erreur de saisie avérée";

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("suivi_scolaire")
            .withUsername("root")
            .withPassword("root");


    // ── datasourceProperties() ───────────────────────────────────────────────

    /**
     * Fournit une base vierge à Spring.
     */

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    /** Horloge déterministe pour les dates de clôture, d'alerte et d'inscription. */
    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AlertesDifficulteService alertesService;


    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Recréé un jeu de données minimal et cohérent avant chaque test.
     */

    @BeforeEach
    void setUp() {
        for (String table : List.of("alerte_difficulte", "historique_reouverture_trimestre",
                "note", "ligne_bulletin", "bulletin", "absence", "cours",
                "attribution_amenagement", "amenagement", "salle", "coefficient",
                "enseignement", "inscription", "eleve", "matiere", "professeur",
                "trimestre", "type_evaluation", "classe", "niveau", "annee_scolaire")) {
            jdbcTemplate.update("DELETE FROM " + table);
        }
        jdbcTemplate.update("INSERT INTO annee_scolaire "
                + "(id_annee, libelle, date_debut, date_fin, seuil_alerte_absences) "
                + "VALUES (2, '2026-2027', DATE '2026-09-01', DATE '2027-06-30', 3)");
        jdbcTemplate.update("INSERT INTO niveau (id_niveau, libelle, code) VALUES "
                + "(1, 'Sixième', '6E'), (2, 'Cinquième', '5E')");
        jdbcTemplate.update("INSERT INTO classe (id_classe, nom, id_annee, id_niveau, effectif_max) VALUES "
                + "(1, '6A', 2, 1, 30), (2, '6B', 2, 1, 30), (3, '5A', 2, 2, 30)");
        jdbcTemplate.update("INSERT INTO eleve (id_eleve, nom, prenom, date_naissance) VALUES "
                + "(1, 'Kouassi', 'Aya', DATE '2011-02-14'), "
                + "(2, 'Dupont', 'Lucas', DATE '2011-05-03'), "
                + "(3, 'Traore', 'Aminata', DATE '2011-08-21'), "
                + "(4, 'Bernard', 'Nina', DATE '2010-11-09')");
        jdbcTemplate.update("INSERT INTO inscription "
                + "(id_inscription, date_inscription, id_eleve, id_classe, numero_inscription) VALUES "
                + "(1, DATE '2026-09-02', 1, 1, 'INS-2026-001'), "
                + "(2, DATE '2026-09-02', 2, 1, 'INS-2026-002'), "
                + "(3, DATE '2026-09-02', 3, 1, 'INS-2026-003'), "
                + "(4, DATE '2026-09-02', 4, 2, 'INS-2026-004')");
        jdbcTemplate.update("INSERT INTO matiere (id_matiere, libelle, code, coefficient) VALUES "
                + "(1, 'Mathematiques', 'MATH', 4), (2, 'Francais', 'FRAN', 3), "
                + "(3, 'Histoire-Geographie', 'HIST', 2)");
        jdbcTemplate.update("INSERT INTO professeur (id_professeur, nom, prenom, email, telephone) VALUES "
                + "(1, 'Durand', 'Claire', 'claire@example.test', '0102030401'), "
                + "(2, 'Koffi', 'Marc', 'marc@example.test', '0102030402')");
        jdbcTemplate.update("INSERT INTO enseignement "
                + "(id_enseignement, id_classe, id_matiere, id_professeur) VALUES "
                + "(1, 1, 1, 1), (2, 1, 2, 2), (3, 1, 3, 1), (4, 2, 1, 2)");
        jdbcTemplate.update("INSERT INTO trimestre "
                + "(id_trimestre, numero, date_debut, date_fin, id_annee) VALUES "
                + "(1, 1, DATE '2026-09-01', DATE '2026-11-15', 2), "
                + "(2, 2, DATE '2026-11-16', DATE '2027-02-15', 2), "
                + "(3, 3, DATE '2027-02-16', DATE '2027-06-30', 2)");
        jdbcTemplate.update("INSERT INTO type_evaluation (id_type_eval, libelle, poids) VALUES "
                + "(1, 'Devoir', 1), (2, 'Interrogation', 2)");
        jdbcTemplate.update("INSERT INTO coefficient (id_coefficient, valeur, id_matiere, code_niveau) VALUES "
                + "(1, 4, 1, '6E'), (2, 3, 2, '6E'), (3, 2, 3, '6E')");
        jdbcTemplate.update("INSERT INTO note "
                + "(id_note, valeur, numero_trimestre, id_type_eval, id_enseignement, numero_inscription) VALUES "
                + "(1, 12, 2, 1, 1, 'INS-2026-001'), "
                + "(2, 14, 2, 2, 2, 'INS-2026-001'), "
                + "(3, 4, 2, 1, 3, 'INS-2026-003'), "
                + "(4, 6, 2, 1, 1, 'INS-2026-002'), "
                + "(5, 8, 2, 2, 2, 'INS-2026-002')");
        alignerSequences();
    }


    // ── alignerSequences() ───────────────────────────────────────────────────

    /**
     * Replace les séquences IDENTITY après les insertions à identifiant
     * explicite du jeu de test.
     */

    private void alignerSequences() {
        for (Map<String, Object> colonne : jdbcTemplate.queryForList(
                "SELECT table_name, column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND identity_generation IS NOT NULL")) {
            String table = (String) colonne.get("table_name");
            String name = (String) colonne.get("column_name");
            jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('" + table + "', '" + name
                    + "'), COALESCE((SELECT MAX(" + name + ") FROM " + table + "), 0) + 1, false)");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SOCLE
    // ═════════════════════════════════════════════════════════════════════════

    // ── migrations() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Les migrations V1 à V4 sont appliquées dans l'ordre")
    void migrations() {
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);

        assertThat(versions).containsExactly("1", "2", "3", "4");
    }

    // ── schemaFinal() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Le schéma final contient 21 identités, 7 vues et les colonnes des arbitrages")
    void schemaFinal() {
        Integer identites = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_attribute a JOIN pg_class c ON c.oid = a.attrelid "
                        + "JOIN pg_namespace n ON n.oid = c.relnamespace "
                        + "WHERE n.nspname = 'public' AND c.relkind = 'r' AND a.attidentity IN ('a','d')",
                Integer.class);
        Integer vues = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_views WHERE schemaname = 'public' AND viewname LIKE 'v_%'",
                Integer.class);
        List<String> colonnesClasse = colonnes("classe");
        List<String> colonnesTrimestre = colonnes("trimestre");

        assertThat(identites).isEqualTo(21);
        assertThat(vues).isEqualTo(7);
        assertThat(colonnesClasse).contains("effectif_max");
        assertThat(colonnesTrimestre).contains("est_cloture", "date_cloture");

        List<String> obligatoires = jdbcTemplate.queryForList(
                "SELECT table_name || '.' || column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND is_nullable = 'NO' AND ("
                        + "(table_name = 'classe' AND column_name = 'effectif_max') OR "
                        + "(table_name = 'matiere' AND column_name = 'coefficient') OR "
                        + "(table_name = 'professeur' AND column_name = 'email'))",
                String.class);
        assertThat(obligatoires).containsExactlyInAnyOrder(
                "classe.effectif_max", "matiere.coefficient", "professeur.email");

        String commentaireRang = jdbcTemplate.queryForObject(
                "SELECT obj_description('v_rang_classe'::regclass)", String.class);
        assertThat(commentaireRang).contains("RANK()", "pas DENSE_RANK()");
    }

    // ── vuesEtSoclePublic() ──────────────────────────────────────────────────

    @Test
    @DisplayName("Les sept vues sont interrogeables et l'API est publique (health)")
    void vuesEtSoclePublic() throws Exception {
        for (String vue : List.of("v_moyenne_matiere", "v_moyenne_generale", "v_rang_classe",
                "v_bulletin", "v_ligne_bulletin", "v_coefficient_manquant",
                "v_absences_a_signaler")) {
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + vue, Integer.class))
                    .as(vue).isNotNull();
        }

        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/suivi-scolaire/classe/list")).andExpect(status().isOk());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CRUD F1 À F5
    // ═════════════════════════════════════════════════════════════════════════

    // ── creerClasse() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la création d'une classe persiste l'effectif maximal")
    void creerClasse() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/classe/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nom", "4A", "idAnnee", 2,
                                "idNiveau", 1, "effectifMax", 28))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.effectifMax").value(28));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT effectif_max FROM classe WHERE nom = '4A'", Integer.class)).isEqualTo(28);
    }

    // ── creerEtRechercherEleve() ─────────────────────────────────────────────

    @Test
    @DisplayName("F2/F9 : créer un élève puis le retrouver par recherche partielle")
    void creerEtRechercherEleve() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/eleve/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nom", "Koffi", "prenom", "Yao",
                                "dateNaissance", "2011-03-02", "idClasse", 2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/suivi-scolaire/eleve/list").param("nom", "KO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Koffi"))
                .andExpect(jsonPath("$[0].nomClasse").value("6B"));
    }

    // ── creerEtLireNote() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F5/F10 : créer une note puis la retrouver dans la page triée")
    void creerEtLireNote() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/note/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idEleve", 1, "idMatiere", 1,
                                "valeur", 18, "trimestre", 2, "idTypeEval", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valeur").value(18));

        mockMvc.perform(get("/suivi-scolaire/note/list")
                        .param("page", "0").param("size", "2").param("sort", "valeur,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].valeur").value(18))
                .andExpect(jsonPath("$.totalElements").value(6));
    }

    // ── noteRefuseeTrimestreCloture() ────────────────────────────────────────

    @Test
    @DisplayName("F5 : aucune note ne peut être saisie dans un trimestre clôturé")
    void noteRefuseeTrimestreCloture() throws Exception {
        jdbcTemplate.update("UPDATE trimestre SET est_cloture = true, date_cloture = DATE '2026-11-16' "
                + "WHERE numero = 1");

        mockMvc.perform(post("/suivi-scolaire/note/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idEleve", 1, "idMatiere", 1,
                                "valeur", 12, "trimestre", 1, "idTypeEval", 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/trimestre-cloture"));
    }

    // ── professeurEmailDoublon() ─────────────────────────────────────────────

    @Test
    @DisplayName("F4 : un email de professeur en doublon est refusé")
    void professeurEmailDoublon() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/professeur/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nom", "Durand", "prenom", "Autre",
                                "email", "claire@example.test", "idMatierePrincipale", 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/professeur-email-deja-utilise"));
    }

    // ── changerClasse() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F11 : le changement de classe conserve l'ancienne inscription")
    void changerClasse() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/eleve/affectation/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idClasseDestination\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreInscriptions").value(2));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT date_fin FROM inscription WHERE id_inscription = 1", LocalDate.class))
                .isEqualTo(LocalDate.of(2026, 11, 24));
    }

    // ── suppressionEleveRefusee() ────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un élève ayant des notes ne peut pas être supprimé")
    void suppressionEleveRefusee() throws Exception {
        mockMvc.perform(delete("/suivi-scolaire/eleve/delete/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("notes")));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RÈGLES MÉTIER F13 À F15
    // ═════════════════════════════════════════════════════════════════════════

    // ── modifierPoids() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F13 : le poids DEV est modifié en base")
    void modifierPoids() throws Exception {
        mockMvc.perform(put("/suivi-scolaire/type-evaluation/poids/DEV")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"poids\":2.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Devoir"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT poids FROM type_evaluation WHERE libelle = 'Devoir'", BigDecimal.class))
                .isEqualByComparingTo("2.5");
    }

    // ── poidsFiges() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("F13 : les poids sont figés dès qu'un trimestre est clôturé")
    void poidsFiges() throws Exception {
        jdbcTemplate.update("UPDATE trimestre SET est_cloture = true WHERE numero = 1");

        mockMvc.perform(put("/suivi-scolaire/type-evaluation/poids/DEV")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"poids\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/poids-fige"));
    }

    // ── clotureIncomplete() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : la clôture incomplète liste la classe et la matière manquantes")
    void clotureIncomplete() throws Exception {
        mockMvc.perform(post("/suivi-scolaire/trimestre/2/cloture"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.suivi-scolaire.local/problems/trimestre-cloture-incomplete"))
                .andExpect(jsonPath("$.manquants[0].nomClasse").value("6B"))
                .andExpect(jsonPath("$.manquants[0].libelleMatiere").value("Mathematiques"));
    }

    // ── cloturePuisReouverture() ─────────────────────────────────────────────

    @Test
    @DisplayName("F14/F15 : clôture complète puis réouverture tracée")
    void cloturePuisReouverture() throws Exception {
        jdbcTemplate.update("INSERT INTO note (valeur, numero_trimestre, id_type_eval, "
                + "id_enseignement, numero_inscription) VALUES (11, 2, 1, 4, 'INS-2026-004')");

        mockMvc.perform(post("/suivi-scolaire/trimestre/2/cloture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreEnseignementsVerifies").value(4));

        mockMvc.perform(post("/suivi-scolaire/trimestre/3/reouverture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("motif", MOTIF, "auteur", "Secrétariat"))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/suivi-scolaire/trimestre/2/reouverture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("motif", MOTIF, "auteur", "Secrétariat"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auteur").value("Secrétariat"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM historique_reouverture_trimestre", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT est_cloture FROM trimestre WHERE numero = 2", Boolean.class)).isFalse();
    }

    // ── reouvertureMotifCourt() ──────────────────────────────────────────────

    @Test
    @DisplayName("F15 : un motif trop court est refusé avant toute écriture")
    void reouvertureMotifCourt() throws Exception {
        jdbcTemplate.update("UPDATE trimestre SET est_cloture = true WHERE numero = 2");

        mockMvc.perform(post("/suivi-scolaire/trimestre/2/reouverture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motif\":\"trop court\",\"auteur\":\"Agent\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM historique_reouverture_trimestre", Integer.class)).isZero();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RESTITUTION F16 À F19
    // ═════════════════════════════════════════════════════════════════════════

    // ── moyennesParMatiere() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F17 : les moyennes par matière distinguent matières évaluées et non évaluées")
    void moyennesParMatiere() throws Exception {
        mockMvc.perform(get("/suivi-scolaire/eleve/moyennes/1").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matieres[0].moyenne").value(12.0))
                .andExpect(jsonPath("$.matieres[0].nombreNotes").value(1))
                .andExpect(jsonPath("$.matieres[1].evaluee").value(true))
                .andExpect(jsonPath("$.matieres[2].evaluee").value(false));
    }

    // ── moyenneGenerale() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F18 : la moyenne générale est pondérée et détaillée par matière")
    void moyenneGenerale() throws Exception {
        mockMvc.perform(get("/suivi-scolaire/eleve/moyenne-generale/1").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moyenneGenerale").isNumber())
                .andExpect(jsonPath("$.detail.length()").value(2));
    }

    // ── classementClasse() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : le classement d'une classe fournit des rangs et une moyenne de classe")
    void classementClasse() throws Exception {
        mockMvc.perform(get("/suivi-scolaire/classe/classement/1").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eleves.length()").value(3))
                .andExpect(jsonPath("$.eleves[0].rang").value(1))
                .andExpect(jsonPath("$.moyenneClasse").isNumber());
    }

    // ── detectionAlertes() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : la détection crée une alerte pour une moyenne faible sans dupliquer l'existante")
    void detectionAlertes() {
        jdbcTemplate.update("INSERT INTO alerte_difficulte "
                + "(id_alerte, id_inscription, numero_trimestre, moyenne, ecart, date_alerte) "
                + "VALUES (100000, 2, 2, 7.00, 0, DATE '2026-11-23')");

        var rapport = alertesService.detecter();

        assertThat(rapport.trimestre()).isEqualTo(2);
        assertThat(rapport.alertesDejaPresentes()).isEqualTo(1);
        assertThat(rapport.alertesCreees()).isGreaterThanOrEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM alerte_difficulte WHERE id_inscription = 2 "
                        + "AND numero_trimestre = 2", Integer.class)).isEqualTo(1);
    }

    // ── consulterAlertes() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : les alertes produites sont consultables par trimestre")
    void consulterAlertes() throws Exception {
        jdbcTemplate.update("INSERT INTO alerte_difficulte "
                + "(id_alerte, id_inscription, numero_trimestre, moyenne, ecart, date_alerte) "
                + "VALUES (100000, 2, 2, 7.00, 1.50, DATE '2026-11-23')");

        mockMvc.perform(get("/suivi-scolaire/alerte/list").param("trimestre", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomEleve").value("Dupont"))
                .andExpect(jsonPath("$[0].moyenne").value(7.0));
    }


    // ── json() ───────────────────────────────────────────────────────────────

    /**
     * Sérialise un corps de requête.
     */

    private String json(Map<String, Object> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }


    // ── colonnes() ───────────────────────────────────────────────────────────

    /**
     * Retourne les colonnes d'une table.
     */

    private List<String> colonnes(String table) {
        return jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = ? ORDER BY ordinal_position",
                String.class, table);
    }
}

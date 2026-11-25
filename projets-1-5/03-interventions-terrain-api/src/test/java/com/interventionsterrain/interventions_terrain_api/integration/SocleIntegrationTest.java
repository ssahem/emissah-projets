package com.interventionsterrain.interventions_terrain_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test de smoke du socle (P0).
 *
 * COUCHE       : Service
 * RÔLE         : Test de smoke du socle (P0).
 * TYPE         : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE    : schéma issu des migrations Flyway V1 à V7, sur une base vierge.
 * CAS COUVERTS : démarrage du contexte, séquence Flyway, identités, index partiel, vues SQL
 *                lisibles, référentiel de statuts, absence de triggers et health.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SocleIntegrationTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("interventions_terrain")
            .withUsername("root")
            .withPassword("root");


    // ── datasourceProperties() ───────────────────────────────────────────────

    /**
     * Fournit une base PostgreSQL vierge à Spring.
     */

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private Clock clock;

    // ── migrationsSequence() ─────────────────────────────────────────────────

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("Les migrations V1 à V7 sont appliquées dans l'ordre")
    void migrationsSequence() {
        // ── Action ───────────────────────────────────────────────────────────
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(versions).containsExactly("1", "2", "3", "4", "5", "6", "7");
    }

    // ── colonnesArbitrees() ──────────────────────────────────────────────────

    @Test
    @DisplayName("Le schéma final porte les colonnes issues des arbitrages")
    void colonnesArbitrees() {
        // ── Action ───────────────────────────────────────────────────────────
        List<String> agence = colonnes("agence");
        List<String> technicien = colonnes("technicien");
        List<String> competence = colonnes("competence");
        List<String> client = colonnes("client");
        List<String> obtention = colonnes("obtention_competence");

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(agence).contains("ville", "responsable");
        assertThat(technicien).contains("matricule");
        assertThat(competence).contains("description");
        assertThat(client).contains("adresse", "commune", "telephone");
        assertThat(obtention).contains("date_alerte");
    }

    // ── identitesEtIndexPartiel() ────────────────────────────────────────────

    @Test
    @DisplayName("Les 19 clés primaires sont des identités et l'index de responsable existe")
    void identitesEtIndexPartiel() {
        // ── Action ───────────────────────────────────────────────────────────
        Integer identites = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_attribute a "
                        + "JOIN pg_class c ON c.oid = a.attrelid "
                        + "JOIN pg_namespace n ON n.oid = c.relnamespace "
                        + "WHERE n.nspname = 'public' AND c.relkind = 'r' AND a.attidentity = 'a'",
                Integer.class);
        Integer index = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes "
                        + "WHERE schemaname = 'public' AND indexname = 'ux_un_responsable_par_intervention'",
                Integer.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(identites).isEqualTo(19);
        assertThat(index).isEqualTo(1);
    }

    // ── vuesLisibles() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Les six vues sont interrogeables sans entité JPA")
    void vuesLisibles() {
        // ── Action + Vérification ────────────────────────────────────────────
        for (String vue : List.of(
                "v_affectation_non_conforme", "v_agence_region", "v_delai_par_agence_mois",
                "v_intervention_delai", "v_intervention_statut", "v_pieces_manquantes")) {
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + vue, Integer.class))
                    .as(vue).isZero();
        }
    }

    // ── statutsSemes() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Le référentiel de statuts contient les cinq libellés de l'énoncé")
    void statutsSemes() {
        // ── Action ───────────────────────────────────────────────────────────
        List<String> libelles = jdbcTemplate.queryForList(
                "SELECT libelle FROM statut ORDER BY libelle", String.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(libelles).containsExactlyInAnyOrder(
                "PLANIFIEE", "EN_COURS", "TERMINEE", "ECHOUEE", "ANNULEE");
    }

    // ── fonctionsEtTriggersEcarter() ─────────────────────────────────────────

    @Test
    @DisplayName("Les fonctions et triggers du schéma source ne sont pas reproduits (E-24)")
    void fonctionsEtTriggersEcarter() {
        // ── Action ───────────────────────────────────────────────────────────
        Integer triggers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_trigger WHERE NOT tgisinternal", Integer.class);
        Integer fonctions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace "
                        + "WHERE n.nspname = 'public'", Integer.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(triggers).isZero();
        assertThat(fonctions).isZero();
    }

    // ── horlogeExposee() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Une horloge applicative est exposée pour les règles temporelles")
    void horlogeExposee() {
        // ── Action + Vérification ────────────────────────────────────────────
        assertThat(clock).isNotNull();
    }

    // ── health() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Le endpoint de health répond UP")
    void health() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }


    // ── colonnes() ───────────────────────────────────────────────────────────

    /**
     * Liste les colonnes d'une table du schéma public.
     */

    private List<String> colonnes(String table) {
        return jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = ?",
                String.class, table);
    }
}

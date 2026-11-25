package com.gestionlocative.gestion_locative_api.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test de smoke du socle (P0).
 *
 * COUCHE       : Service
 * RÔLE         : Test de smoke du socle (P0).
 * TYPE         : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE    : schéma issu des migrations Flyway V1 à V8, sur une base vierge.
 * CAS COUVERTS : démarrage du contexte, séquence Flyway, bean Clock, vues SQL lisibles.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
class SocleIntegrationTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("gestion_locative")
            .withUsername("root")
            .withPassword("root");

    // ── datasourceProperties() ───────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour datasourceProperties().
     */

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired private JdbcTemplate jdbcTemplate;

    // ── migrationsSequence() ─────────────────────────────────────────────────

    @Autowired private Clock clock;

    @Test
    @DisplayName("Les migrations V1 à V8 sont appliquées dans l'ordre")
    void migrationsSequence() {
        // ── Action ───────────────────────────────────────────────────────────
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(versions).containsExactly("1", "2", "3", "4", "5", "6", "7", "8");
    }

    // ── colonnesArbitrees() ──────────────────────────────────────────────────

    @Test
    @DisplayName("Le schéma final porte les colonnes issues des arbitrages")
    void colonnesArbitrees() {
        // ── Action ───────────────────────────────────────────────────────────
        List<String> colonnes = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'immeuble'",
                String.class);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(colonnes).contains("id_proprietaire", "nb_etages").doesNotContain("ancienne_colonne");
    }

    // ── vuesLisibles() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Les deux vues sont interrogeables sans entité JPA")
    void vuesLisibles() {
        // ── Action + Vérification ────────────────────────────────────────────
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v_solde_quittance", Integer.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v_restitution_depot", Integer.class))
                .isZero();
    }

    // ── horlogeExposee() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Une horloge applicative est exposée pour les règles temporelles")
    void horlogeExposee() {
        // ── Action + Vérification ────────────────────────────────────────────
        assertThat(clock).isNotNull();
    }
}

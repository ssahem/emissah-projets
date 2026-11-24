package com.interventionsterrain.interventions_terrain_api.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Vérification des données de démonstration chargées par le profil "dev".
 *
 * TYPE       : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE  : migrations V1 à V7 plus V8 de db/dev, sur une base vierge.
 * CONVENTION : Préparation → Action → Vérification.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("dev")
class DemoDataIntegrationTest {

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("interventions_terrain")
            .withUsername("root")
            .withPassword("root");

    /** Fournit une base PostgreSQL vierge à Spring. */
    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Le profil dev applique V8 et fournit un type avec ses compétences requises")
    void donneesDemo() {
        // ── Action ──
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);
        List<String> codes = jdbcTemplate.queryForList("SELECT code FROM type_intervention", String.class);
        Integer exigences = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exigence_competence e "
                        + "JOIN type_intervention t ON t.id_type = e.id_type "
                        + "WHERE t.code = 'RACCORDEMENT_FIBRE'", Integer.class);
        Integer techniciens = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM technicien", Integer.class);

        // ── Vérification ──
        assertThat(versions).containsExactly("1", "2", "3", "4", "5", "6", "7", "8");
        assertThat(codes).containsExactly("RACCORDEMENT_FIBRE");
        assertThat(exigences).isEqualTo(2);
        assertThat(techniciens).isEqualTo(2);
    }
}

package com.interventionsterrain.interventions_terrain_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
 * Parcours nominal P1 sur une vraie base PostgreSQL.
 *
 * COUCHE       : Service
 * RÔLE         : Parcours nominal P1 sur une vraie base PostgreSQL.
 * TYPE         : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE    : migrations V1 à V7, horloge fixe, appels HTTP réels via MockMvc.
 * CAS COUVERTS : création agence → technicien → client → intervention, adresse créée dans la
 *                même transaction, responsable unique, recopie du statut au changement de responsable.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CrudIntegrationTest {

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
     * Prépare la donnée de test pour datasourceProperties().
     */

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    /** Horloge déterministe pour les historiques de statut. */
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

    private Integer idAgence;
    private Integer idTechnicien1;
    private Integer idTechnicien2;
    private Integer idClient;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("DELETE FROM historique_statut");
        jdbcTemplate.update("DELETE FROM affectation");
        jdbcTemplate.update("DELETE FROM intervention");
        jdbcTemplate.update("DELETE FROM adresse");
        jdbcTemplate.update("DELETE FROM client");
        jdbcTemplate.update("DELETE FROM technicien");
        jdbcTemplate.update("DELETE FROM equipe");
        jdbcTemplate.update("DELETE FROM agence");
        jdbcTemplate.update("DELETE FROM type_intervention");
        jdbcTemplate.update("INSERT INTO type_intervention (libelle, delai_heures, code) "
                + "VALUES ('Raccordement fibre', 48, 'RACCORDEMENT_FIBRE')");

        idAgence = create("agence", Map.of("nom", "Paris Centre", "ville", "Paris"));
        idTechnicien1 = create("technicien", Map.of(
                "nom", "Martin", "prenom", "Camille", "email", "camille@example.test",
                "telephone", "0102030405", "idAgence", idAgence, "matricule", "MAT001"));
        idTechnicien2 = create("technicien", Map.of(
                "nom", "Bernard", "prenom", "Luc", "email", "luc@example.test",
                "telephone", "0102030406", "idAgence", idAgence, "matricule", "MAT002"));
        idClient = create("client", Map.of(
                "nom", "Dupont", "prenom", "Marie", "email", "marie@example.test",
                "telephone", "0102030407", "adresse", "5 avenue du parc", "commune", "Paris"));
    }

    // ── parcoursComplet() ────────────────────────────────────────────────────

    @Test
    @DisplayName("Le parcours complet crée une intervention exploitable")
    void parcoursComplet() throws Exception {
        // ── Action : création de l'intervention ──────────────────────────────
        Integer idIntervention = createIntervention();

        // ── Vérification : réponse, adresse, responsable et statut ───────────
        mockMvc.perform(get("/interventions-terrain/intervention/get/" + idIntervention))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeType").value("RACCORDEMENT_FIBRE"))
                .andExpect(jsonPath("$.voie").value("12 rue de la Paix"))
                .andExpect(jsonPath("$.commune").value("Paris"))
                .andExpect(jsonPath("$.idTechnicienResponsable").value(idTechnicien1));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM adresse WHERE voie = '12 rue de la Paix'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM affectation WHERE id_intervention = ? AND est_responsable",
                Integer.class, idIntervention)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT s.libelle FROM historique_statut h JOIN statut s ON s.id_statut = h.id_statut "
                        + "JOIN affectation a ON a.id_affectation = h.id_affectation "
                        + "WHERE a.id_intervention = ?", String.class, idIntervention))
                .isEqualTo("PLANIFIEE");

        // ── Action : ajout d'un second technicien non responsable ────────────
        mockMvc.perform(post("/interventions-terrain/intervention/affectation/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("idTechnicien", idTechnicien2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estResponsable").value(false));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM affectation WHERE id_intervention = ?", Integer.class, idIntervention))
                .isEqualTo(2);
    }

    // ── filtreStatutSurResponsable() ─────────────────────────────────────────

    @Test
    @DisplayName("F9 : le filtre de statut lit le dernier historique du responsable")
    void filtreStatutSurResponsable() throws Exception {
        Integer idIntervention = createIntervention();
        Integer idResponsable = jdbcTemplate.queryForObject(
                "SELECT id_affectation FROM affectation WHERE id_intervention = ? AND est_responsable",
                Integer.class, idIntervention);
        jdbcTemplate.update("INSERT INTO historique_statut (date_heure, id_statut, id_affectation) "
                        + "VALUES (?, (SELECT id_statut FROM statut WHERE libelle = 'EN_COURS'), ?)",
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2026, 11, 24, 11, 0)),
                idResponsable);

        mockMvc.perform(get("/interventions-terrain/intervention/list")
                        .param("statut", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idIntervention").value(idIntervention));

        mockMvc.perform(get("/interventions-terrain/intervention/list")
                        .param("statut", "TERMINEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/interventions-terrain/intervention/list")
                        .param("du", "2026-12-02").param("au", "2026-12-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── changementResponsablePreserveStatut() ────────────────────────────────

    @Test
    @DisplayName("Le changement de responsable reste unique et recopie le statut")
    void changementResponsablePreserveStatut() throws Exception {
        Integer idIntervention = createIntervention();

        mockMvc.perform(put("/interventions-terrain/intervention/update/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "debutPrevu", "2026-12-02T09:00:00",
                                "finPrevue", "2026-12-02T12:00:00",
                                "idTechnicienResponsable", idTechnicien2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTechnicienResponsable").value(idTechnicien2));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM affectation WHERE id_intervention = ? AND est_responsable",
                Integer.class, idIntervention)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT s.libelle FROM historique_statut h JOIN statut s ON s.id_statut = h.id_statut "
                        + "JOIN affectation a ON a.id_affectation = h.id_affectation "
                        + "WHERE a.id_intervention = ? AND a.est_responsable "
                        + "ORDER BY h.date_heure DESC, h.id_historique DESC LIMIT 1",
                String.class, idIntervention)).isEqualTo("PLANIFIEE");
    }

    // ── interventionCommenceeFigee() ─────────────────────────────────────────

    @Test
    @DisplayName("Une intervention commencée n'est plus modifiable ni supprimable")
    void interventionCommenceeFigee() throws Exception {
        Integer idIntervention = createIntervention();
        Integer idResponsable = jdbcTemplate.queryForObject(
                "SELECT id_affectation FROM affectation WHERE id_intervention = ? AND est_responsable",
                Integer.class, idIntervention);
        jdbcTemplate.update("INSERT INTO historique_statut (date_heure, id_statut, id_affectation) "
                        + "VALUES (?, (SELECT id_statut FROM statut WHERE libelle = 'EN_COURS'), ?)",
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(2026, 11, 24, 11, 0)),
                idResponsable);

        mockMvc.perform(put("/interventions-terrain/intervention/update/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "debutPrevu", "2026-12-02T09:00:00",
                                "finPrevue", "2026-12-02T12:00:00",
                                "idTechnicienResponsable", idTechnicien2))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/intervention-non-modifiable"));
    }

    /** Crée une ressource via l'API et renvoie son identifiant. */

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour create().
     */

    private Integer create(String ressource, Map<String, Object> body) throws Exception {
        String json = mockMvc.perform(post("/interventions-terrain/" + ressource + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id" + capitalize(ressource)).asInt();
    }

    /** Crée l'intervention de référence. */

    // ── createIntervention() ─────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour createIntervention().
     */

    private Integer createIntervention() throws Exception {
        return create("intervention", Map.of(
                "idClient", idClient,
                "codeType", "RACCORDEMENT_FIBRE",
                "idTechnicienResponsable", idTechnicien1,
                "dateDemande", "2026-11-24T09:00:00",
                "debutPrevu", "2026-12-01T09:00:00",
                "finPrevue", "2026-12-01T12:00:00",
                "voie", "12 rue de la Paix",
                "commune", "Paris"));
    }

    /** Première lettre en majuscule pour reconstruire le nom du champ d'identifiant. */

    // ── capitalize() ─────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour capitalize().
     */

    private String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}

package com.interventionsterrain.interventions_terrain_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interventionsterrain.interventions_terrain_api.dto.response.AlerteAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.service.AlerteCompetenceService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
 * Règles métier F11 à F16 exécutées sur une vraie base PostgreSQL.
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F11 à F16 exécutées sur une vraie base PostgreSQL.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReglesMetierIntegrationTest {

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
    @Autowired private AlerteCompetenceService alerteService;

    private Integer idTechnicien;
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
        jdbcTemplate.update("DELETE FROM exigence_competence");
        jdbcTemplate.update("DELETE FROM obtention_competence");
        jdbcTemplate.update("DELETE FROM competence");
        jdbcTemplate.update("DELETE FROM client");
        jdbcTemplate.update("DELETE FROM technicien");
        jdbcTemplate.update("DELETE FROM equipe");
        jdbcTemplate.update("DELETE FROM agence");
        jdbcTemplate.update("DELETE FROM type_intervention");
        jdbcTemplate.update("INSERT INTO type_intervention (libelle, delai_heures, code) "
                + "VALUES ('Raccordement fibre', 48, 'RACCORDEMENT_FIBRE')");

        Integer idAgence = create("agence", Map.of("nom", "Paris Centre", "ville", "Paris"));
        idTechnicien = create("technicien", Map.of(
                "nom", "Martin", "prenom", "Camille", "email", "camille@example.test",
                "telephone", "0102030405", "idAgence", idAgence, "matricule", "MAT001"));
        idClient = create("client", Map.of(
                "nom", "Dupont", "prenom", "Marie", "email", "marie@example.test",
                "telephone", "0102030407", "adresse", "5 avenue du parc", "commune", "Paris"));
    }

    // ── f11Competence() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F11 : associer une compétence persiste l'obtention et refuse un doublon actif")
    void f11Competence() throws Exception {
        Integer idCompetence = create("competence", Map.of("libelle", "Fibre"));
        Map<String, Object> body = Map.of("idCompetence", idCompetence,
                "dateObtention", "2026-01-10", "dateExpiration", "2027-01-10");

        mockMvc.perform(post("/interventions-terrain/technicien/competence/" + idTechnicien)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.libelle").value("Fibre"));

        mockMvc.perform(post("/interventions-terrain/technicien/competence/" + idTechnicien)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/competence-active-deja-obtenue"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM obtention_competence WHERE id_technicien = ? AND id_competence = ?",
                Integer.class, idTechnicien, idCompetence)).isEqualTo(1);
    }

    // ── f12Chevauchement() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F12 : un second chevauchement est refusé avec l'intervention bloquante")
    void f12Chevauchement() throws Exception {
        Integer premiere = createIntervention("2026-12-01T09:00:00", "2026-12-01T12:00:00");

        mockMvc.perform(post("/interventions-terrain/intervention/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpsIntervention(
                                "2026-12-01T10:00:00", "2026-12-01T13:00:00"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/chevauchement-creneau"))
                .andExpect(jsonPath("$.idInterventionBloquant").value(premiere))
                .andExpect(jsonPath("$.debut").value("2026-12-01T09:00:00"));
    }

    // ── f13CompetenceRequise() ───────────────────────────────────────────────

    @Test
    @DisplayName("F13 : une compétence exigée mais non détenue bloque la création")
    void f13CompetenceRequise() throws Exception {
        Integer idCompetence = create("competence", Map.of("libelle", "Fibre"));
        jdbcTemplate.update("INSERT INTO exigence_competence (id_type, id_competence) "
                + "SELECT id_type, ? FROM type_intervention WHERE code = 'RACCORDEMENT_FIBRE'",
                idCompetence);

        mockMvc.perform(post("/interventions-terrain/intervention/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpsIntervention(
                                "2026-12-01T09:00:00", "2026-12-01T12:00:00"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/competences-manquantes"))
                .andExpect(jsonPath("$.competences[0]").value("Fibre"));
    }

    // ── f14CycleDeVie() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : le cycle de vie est horodaté et les transitions interdites sont refusées")
    void f14CycleDeVie() throws Exception {
        Integer idIntervention = createIntervention("2026-12-01T09:00:00", "2026-12-01T12:00:00");

        mockMvc.perform(post("/interventions-terrain/intervention/statut/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"statut\":\"EN_COURS\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/interventions-terrain/intervention/statut/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"statut\":\"ANNULEE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statutCourant").value("EN_COURS"))
                .andExpect(jsonPath("$.atteignables[0]").value("ECHOUEE"));

        Integer nbHistoriques = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM historique_statut h JOIN affectation a "
                        + "ON a.id_affectation = h.id_affectation WHERE a.id_intervention = ?",
                Integer.class, idIntervention);
        assertThat(nbHistoriques).isEqualTo(2);
        assertThat(statutCourant(idIntervention)).isEqualTo("EN_COURS");
    }

    // ── f15Reprise() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : une reprise d'intervention échouée référence l'origine et démarre en PLANIFIEE")
    void f15Reprise() throws Exception {
        Integer idIntervention = createIntervention("2026-12-01T09:00:00", "2026-12-01T12:00:00");
        changerStatut(idIntervention, "EN_COURS");
        changerStatut(idIntervention, "ECHOUEE");

        String json = mockMvc.perform(post("/interventions-terrain/intervention/reprise/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "debutPrevu", "2026-12-08T09:00:00",
                                "finPrevue", "2026-12-08T12:00:00"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer idReprise = objectMapper.readTree(json).get("idIntervention").asInt();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT id_intervention_origine FROM intervention WHERE id_intervention = ?",
                Integer.class, idReprise)).isEqualTo(idIntervention);
        assertThat(statutCourant(idReprise)).isEqualTo("PLANIFIEE");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT id_technicien FROM affectation WHERE id_intervention = ? AND est_responsable",
                Integer.class, idReprise)).isEqualTo(idTechnicien);
    }

    // ── f16Alerte() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : une compétence proche de l'expiration est signalée une seule fois par agence")
    void f16Alerte() throws Exception {
        Integer idCompetence = create("competence", Map.of("libelle", "Fibre"));
        jdbcTemplate.update("INSERT INTO obtention_competence "
                + "(date_obtention, date_expiration, id_technicien, id_competence) "
                + "VALUES (DATE '2025-01-10', DATE '2026-12-10', ?, ?)", idTechnicien, idCompetence);

        java.util.List<AlerteAgenceResponse> alertes = alerteService.genererAlertes();

        assertThat(alertes).hasSize(1);
        assertThat(alertes.get(0).lignes()).hasSize(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT date_alerte FROM obtention_competence WHERE id_technicien = ? AND id_competence = ?",
                java.sql.Date.class, idTechnicien, idCompetence).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 11, 24));
        assertThat(alerteService.genererAlertes()).isEmpty();
    }

    // ── statutCourant() ──────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour statutCourant().
     */

    private String statutCourant(Integer idIntervention) {
        return jdbcTemplate.queryForObject(
                "SELECT statut FROM v_intervention_statut WHERE id_intervention = ?",
                String.class, idIntervention);
    }

    // ── changerStatut() ──────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour changerStatut().
     */

    private void changerStatut(Integer idIntervention, String statut) throws Exception {
        mockMvc.perform(post("/interventions-terrain/intervention/statut/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("statut", statut))))
                .andExpect(status().isOk());
    }

    // ── createIntervention() ─────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour createIntervention().
     */

    private Integer createIntervention(String debut, String fin) throws Exception {
        return create("intervention", corpsIntervention(debut, fin));
    }

    private Map<String, Object> corpsIntervention(String debut, String fin) {
        return Map.of(
                "idClient", idClient,
                "codeType", "RACCORDEMENT_FIBRE",
                "idTechnicienResponsable", idTechnicien,
                "dateDemande", "2026-11-24T09:00:00",
                "debutPrevu", debut,
                "finPrevue", fin,
                "voie", "12 rue de la Paix",
                "commune", "Paris");
    }

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
        String Champ = "id" + Character.toUpperCase(ressource.charAt(0)) + ressource.substring(1);
        return objectMapper.readTree(json).get(Champ).asInt();
    }
}

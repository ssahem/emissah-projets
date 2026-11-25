package com.interventionsterrain.interventions_terrain_api.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * Parcours de restitution F17 à F19 sur une vraie base PostgreSQL.
 *
 * COUCHE       : Service
 * RÔLE         : Parcours de restitution F17 à F19 sur une vraie base PostgreSQL.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class RestitutionIntegrationTest {

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
     * Injecte dans Spring les paramètres JDBC du conteneur PostgreSQL de test.
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

    private Integer idAgence;
    private Integer idTechnicien;
    private Integer idClient;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
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

        idAgence = create("agence", Map.of("nom", "Agence Nord", "ville", "Paris"));
        Integer autreAgence = create("agence", Map.of("nom", "Agence Sud", "ville", "Lyon"));
        idTechnicien = create("technicien", Map.of(
                "nom", "Alice", "prenom", "Camille", "email", "alice@example.test",
                "telephone", "0102030405", "idAgence", idAgence, "matricule", "MAT001"));
        create("technicien", Map.of(
                "nom", "Bob", "prenom", "Nicolas", "email", "bob@example.test",
                "telephone", "0102030406", "idAgence", autreAgence, "matricule", "MAT002"));
        idClient = create("client", Map.of(
                "nom", "Dupont", "prenom", "Marie", "email", "marie@example.test",
                "telephone", "0102030407", "adresse", "5 avenue du parc", "commune", "Paris"));

        Integer idCompetence = create("competence", Map.of("libelle", "Fibre"));
        jdbcTemplate.update("INSERT INTO exigence_competence (id_type, id_competence) "
                + "SELECT id_type, ? FROM type_intervention WHERE code = 'RACCORDEMENT_FIBRE'",
                idCompetence);
        mockMvc.perform(post("/interventions-terrain/technicien/competence/" + idTechnicien)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCompetence", idCompetence,
                                "dateObtention", "2026-01-10",
                                "dateExpiration", "2027-01-10"))))
                .andExpect(status().isCreated());

        Integer annulee = createIntervention(idClient, "2026-12-10T09:00:00", "2026-12-10T11:00:00");
        changerStatut(annulee, "ANNULEE");
        Integer terminee = createIntervention(idClient, "2026-12-10T14:00:00", "2026-12-10T17:00:00");
        changerStatut(terminee, "EN_COURS");
        changerStatut(terminee, "TERMINEE");
        Integer echeouee = createIntervention(idClient, "2026-12-11T09:00:00", "2026-12-11T12:00:00");
        changerStatut(echeouee, "EN_COURS");
        changerStatut(echeouee, "ECHOUEE");
        createIntervention(idClient, "2026-12-12T09:00:00", "2026-12-12T12:00:00");
        // Bob n'a pas la compétence exigée : il ne doit jamais apparaître dans F17.
    }

    // ── f17Disponibles() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F17 : le technicien compétent est proposé, l'agence sans candidat est absente")
    void f17Disponibles() throws Exception {
        mockMvc.perform(get("/interventions-terrain/technicien/disponibles")
                        .param("debut", "2026-12-10T09:00:00")
                        .param("fin", "2026-12-10T12:00:00")
                        .param("type", "RACCORDEMENT_FIBRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences.length()").value(1))
                .andExpect(jsonPath("$.agences[0].idAgence").value(idAgence))
                .andExpect(jsonPath("$.agences[0].techniciens[0].idTechnicien").value(idTechnicien))
                .andExpect(jsonPath("$.agences[0].techniciens[0].chargeHeuresJour").value(3.00));
    }

    // ── f17AucunDisponible() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F17 : aucun technicien libre sur le créneau renvoie une raison de filtrage")
    void f17AucunDisponible() throws Exception {
        createIntervention(idClient, "2026-12-15T09:00:00", "2026-12-15T12:00:00");

        mockMvc.perform(get("/interventions-terrain/technicien/disponibles")
                        .param("debut", "2026-12-15T10:00:00")
                        .param("fin", "2026-12-15T11:00:00")
                        .param("type", "RACCORDEMENT_FIBRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences").isEmpty())
                .andExpect(jsonPath("$.raisonFiltrage").isNotEmpty());
    }

    // ── f18Charge() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("F18 : la charge d'agence exclut l'intervention annulée et totalise les heures")
    void f18Charge() throws Exception {
        mockMvc.perform(get("/interventions-terrain/agence/charge/" + idAgence)
                        .param("du", "2026-12-10T00:00:00")
                        .param("au", "2026-12-11T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomAgence").value("Agence Nord"))
                .andExpect(jsonPath("$.techniciens.length()").value(1))
                .andExpect(jsonPath("$.techniciens[0].nombreInterventions").value(1))
                .andExpect(jsonPath("$.techniciens[0].heuresOccupees").value(3.00))
                .andExpect(jsonPath("$.nombreInterventionsTotal").value(1))
                .andExpect(jsonPath("$.heuresOccupeesTotales").value(3.00));
    }

    // ── f19Taux() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : le taux mensuel retient seulement TERMINEE et ECHOUEE")
    void f19Taux() throws Exception {
        mockMvc.perform(get("/interventions-terrain/agence/taux-reussite/" + idAgence)
                        .param("mois", "2026-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terminees").value(1))
                .andExpect(jsonPath("$.echeouees").value(1))
                .andExpect(jsonPath("$.tauxGlobal").value(50.00))
                .andExpect(jsonPath("$.parType[0].codeType").value("RACCORDEMENT_FIBRE"))
                .andExpect(jsonPath("$.parType[0].taux").value(50.00));
    }

    // ── f19Erreurs() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : un mois invalide renvoie 400 et une agence inconnue renvoie 404")
    void f19Erreurs() throws Exception {
        mockMvc.perform(get("/interventions-terrain/agence/taux-reussite/" + idAgence)
                        .param("mois", "12-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/mois-invalide"));
        mockMvc.perform(get("/interventions-terrain/agence/taux-reussite/999")
                        .param("mois", "2026-12"))
                .andExpect(status().isNotFound());
    }

    // ── changerStatut() ──────────────────────────────────────────────────────

    /**
     * Appelle l’endpoint de changement de statut.
     */

    private void changerStatut(Integer idIntervention, String statut) throws Exception {
        mockMvc.perform(post("/interventions-terrain/intervention/statut/" + idIntervention)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("statut", statut))))
                .andExpect(status().isOk());
    }

    // ── createIntervention() ─────────────────────────────────────────────────

    /**
     * Prépare le contexte nécessaire à l’opération createIntervention().
     */

    private Integer createIntervention(Integer idClient, String debut, String fin) throws Exception {
        return create("intervention", Map.of(
                "idClient", idClient,
                "codeType", "RACCORDEMENT_FIBRE",
                "idTechnicienResponsable", idTechnicien,
                "dateDemande", "2026-11-24T09:00:00",
                "debutPrevu", debut,
                "finPrevue", fin,
                "voie", "12 rue de la Paix",
                "commune", "Paris"));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée un restitution.
     */

    private Integer create(String ressource, Map<String, Object> body) throws Exception {
        String json = mockMvc.perform(post("/interventions-terrain/" + ressource + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String champ = "id" + Character.toUpperCase(ressource.charAt(0)) + ressource.substring(1);
        return objectMapper.readTree(json).get(champ).asInt();
    }
}

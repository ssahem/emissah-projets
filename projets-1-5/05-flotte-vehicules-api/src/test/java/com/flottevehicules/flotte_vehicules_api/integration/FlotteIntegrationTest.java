package com.flottevehicules.flotte_vehicules_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
 * Parcours métier complets sur une vraie base PostgreSQL.
 *
 * TYPE         : Intégration (Testcontainers PostgreSQL 16, horloge fixe).
 * STRATÉGIE    : migrations V1 à V3, données de référence insérées en SQL, appels HTTP via MockMvc.
 * CAS COUVERTS : F1 à F19 hors tâche planifiée réelle, D-01 à D-03 et arbitrages A-01/A-02.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class FlotteIntegrationTest {

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("flotte_vehicules")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    /** Horloge déterministe : toute la suite se déroule le 24 novembre 2026. */
    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Integer idSite;
    private Integer idSalarie;
    private Integer idTypeEntretien;
    private Integer idPrestataire;

    @BeforeEach
    void setUp() throws Exception {
        // Préparation : une base vide et les référentiels nécessaires aux scénarios.
        jdbcTemplate.update("DELETE FROM alerte_vehicule");
        jdbcTemplate.update("DELETE FROM trajet");
        jdbcTemplate.update("DELETE FROM entretien");
        jdbcTemplate.update("DELETE FROM affectation");
        jdbcTemplate.update("DELETE FROM depense");
        jdbcTemplate.update("DELETE FROM contrat");
        jdbcTemplate.update("DELETE FROM vehicule");
        jdbcTemplate.update("DELETE FROM salarie");
        jdbcTemplate.update("DELETE FROM site");
        jdbcTemplate.update("DELETE FROM type_entretien");
        jdbcTemplate.update("DELETE FROM prestataire");
        jdbcTemplate.update("DELETE FROM type_depense");
        jdbcTemplate.update("INSERT INTO type_entretien "
                + "(libelle, periodicite_km, periodicite_mois, categorie) "
                + "VALUES ('Vidange périodique', 10000, 12, 'PERIODIQUE')");
        jdbcTemplate.update("INSERT INTO prestataire (nom, adresse, telephone) "
                + "VALUES ('Garage Central', '12 rue du Garage', '0102030405')");

        idSite = creer("site", Map.of("nom", "Paris Centre", "ville", "Paris",
                "adresse", "12 rue de la Paix"));
        idSalarie = creer("salarie", Map.of("nom", "Martin", "prenom", "Alice",
                "email", "alice@example.test", "matricule", "MAT001", "service", "Direction"));
        idTypeEntretien = jdbcTemplate.queryForObject(
                "SELECT id_type_entretien FROM type_entretien WHERE libelle = ?",
                Integer.class, "Vidange périodique");
        idPrestataire = jdbcTemplate.queryForObject(
                "SELECT id_prestataire FROM prestataire WHERE nom = ?",
                Integer.class, "Garage Central");
    }

    @Test
    @DisplayName("Le parcours complet couvre la création, l'affectation, la restitution et le coût")
    void parcoursComplet() throws Exception {
        // Préparation
        Integer idVehicule = creerVehicule("AB-123-CD");

        // Action
        Integer idAffectation = creer("affectation", Map.of(
                "idVehicule", idVehicule, "idSalarie", idSalarie,
                "dateDebut", "2026-11-01"));
        mockMvc.perform(post("/flotte-vehicules/affectation/" + idAffectation + "/restitution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("date", "2026-11-23", "km", 19000))))
                .andExpect(status().isOk());
        Integer idEntretien = creer("entretien", Map.of(
                "date", "2026-11-25", "idVehicule", idVehicule,
                "idTypeEntretien", idTypeEntretien, "idPrestataire", idPrestataire,
                "cout", 320, "km", 19100));

        // Vérification : la restitution est persistée et l'entretien est exploitable
        mockMvc.perform(get("/flotte-vehicules/affectation/" + idAffectation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kmRestitution").value(19000))
                .andExpect(jsonPath("$.enCours").value(false));
        mockMvc.perform(get("/flotte-vehicules/entretien/" + idEntretien))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelleType").value("Vidange périodique"));
        mockMvc.perform(get("/flotte-vehicules/vehicule/" + idVehicule + "/cout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coutTotal").value(320));
        mockMvc.perform(get("/flotte-vehicules/vehicule/" + idVehicule + "/entretiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coutCumule").value(320))
                .andExpect(jsonPath("$.entretiens.length()").value(1));
        mockMvc.perform(get("/flotte-vehicules/site/" + idSite + "/kilometrage")
                        .param("mois", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kmTotal").value(19000));
    }

    @Test
    @DisplayName("F11, F12 et F14 : chevauchement, conflit et compteur décroissant sont refusés")
    void reglesMetierAffectationEntretien() throws Exception {
        // Préparation
        Integer idVehicule = creerVehicule("CD-456-EF");
        Integer idAffectation = creer("affectation", Map.of(
                "idVehicule", idVehicule, "idSalarie", idSalarie,
                "dateDebut", "2026-11-01"));

        // Action et vérification : une affectation qui recouvre la première est refusée
        mockMvc.perform(post("/flotte-vehicules/affectation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idVehicule", idVehicule, "idSalarie", idSalarie,
                                "dateDebut", "2026-11-05", "dateFin", "2026-11-15"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflit métier"));

        // Action et vérification : un entretien pendant l'affectation crée une alerte F12
        mockMvc.perform(post("/flotte-vehicules/entretien/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("date", "2026-11-05", "idVehicule", idVehicule,
                                "idTypeEntretien", idTypeEntretien, "idPrestataire", idPrestataire,
                                "cout", 150, "km", 15000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alerteCreee").value(true));
        mockMvc.perform(get("/flotte-vehicules/alerte/list").param("idVehicule", idVehicule.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motif").value("CONFLIT_AFFECTATION_ENTRETIEN"));

        // Action et vérification : D-01 refuse un relevé plus faible, accepte un relevé égal
        mockMvc.perform(post("/flotte-vehicules/affectation/" + idAffectation + "/restitution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("date", "2026-11-24", "km", 14000))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/flotte-vehicules/affectation/" + idAffectation + "/restitution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("date", "2026-11-24", "km", 15000))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kmRestitution").value(15000));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM alerte_vehicule WHERE id_vehicule = ?",
                Integer.class, idVehicule)).isEqualTo(1);
    }

    @Test
    @DisplayName("F16 : les alertes sont générées une seule fois et regroupées par site")
    void generationAlertesNonDupliquees() throws Exception {
        // Préparation : deux véhicules sur deux sites, tous deux en retard d'échéance
        creerVehicule("EF-789-GH");
        Integer idSiteLyon = creer("site", Map.of("nom", "Lyon Part-Dieu", "ville", "Lyon",
                "adresse", "5 rue de la République"));
        creer("vehicule", Map.of("immatriculation", "GH-012-IJ", "marque", "Renault",
                "modele", "Clio", "dateMiseEnService", "2024-01-15", "idSite", idSiteLyon,
                "modeAcquisition", "LEASING", "partage", false));

        // Action : deux générations successives
        mockMvc.perform(post("/flotte-vehicules/alerte/generer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(post("/flotte-vehicules/alerte/generer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Vérification : aucune alerte dupliquée et regroupement cohérent
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM alerte_vehicule WHERE motif = 'ECHEANCE_ENTRETIEN'",
                Integer.class)).isEqualTo(2);
        mockMvc.perform(get("/flotte-vehicules/alerte/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nomSite").exists());
        mockMvc.perform(get("/flotte-vehicules/vue/v_echeance_entretien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/flotte-vehicules/vue/inconnue"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("F17 : un véhicule affecté sur la période est exclu des disponibilités")
    void disponibilitesSurPeriode() throws Exception {
        // Préparation
        Integer idLibre = creerVehicule("IJ-345-KL");
        Integer idOccupee = creerVehicule("KL-678-MN");
        creer("affectation", Map.of("idVehicule", idOccupee, "idSalarie", idSalarie,
                "dateDebut", "2026-11-23", "dateFin", "2026-11-29"));

        // Action
        mockMvc.perform(get("/flotte-vehicules/vehicule/disponibles")
                        .param("du", "2026-11-23").param("au", "2026-11-29"))
                // Vérification
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idSite").value(idSite))
                .andExpect(jsonPath("$[0].vehicules.length()").value(1))
                .andExpect(jsonPath("$[0].vehicules[0].idVehicule").value(idLibre));
    }

    @Test
    @DisplayName("F9 et F15 : la recherche combinée et la prochaine échéance sont opérationnelles")
    void rechercheEtEcheance() throws Exception {
        // Préparation
        Integer idVehicule = creerVehicule("MN-901-OP");

        // Action et vérification
        mockMvc.perform(get("/flotte-vehicules/vehicule/list")
                        .param("immatriculation", "MN-901")
                        .param("marque", "Peugeot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idVehicule").value(idVehicule));
        mockMvc.perform(get("/flotte-vehicules/vehicule/" + idVehicule + "/prochain-entretien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelleType").value("Vidange périodique"))
                .andExpect(jsonPath("$.kmCible").value(10000));
        mockMvc.perform(get("/flotte-vehicules/salarie/" + idSalarie + "/affectations"))
                .andExpect(status().isOk());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ══════════════════════════════════════════════════════════════════════════

    /** Crée une ressource via l'API et retourne son identifiant. */
    private Integer creer(String ressource, Map<String, Object> corps) throws Exception {
        String contenu = mockMvc.perform(post("/flotte-vehicules/" + ressource + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(corps)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String champ = switch (ressource) {
            case "site" -> "idSite";
            case "salarie" -> "idSalarie";
            case "vehicule" -> "idVehicule";
            case "affectation" -> "idAffectation";
            case "entretien" -> "idEntretien";
            default -> throw new IllegalArgumentException("Ressource inconnue : " + ressource);
        };
        return jsonMapper.readTree(contenu).get(champ).asInt();
    }

    /** Crée un véhicule de référence, en propriété, sur le site de Paris. */
    private Integer creerVehicule(String immatriculation) throws Exception {
        return creer("vehicule", Map.of(
                "immatriculation", immatriculation, "marque", "Peugeot", "modele", "308",
                "dateMiseEnService", "2024-01-15", "idSite", idSite,
                "modeAcquisition", "PROPRIETE", "partage", false, "prixAchat", 25000));
    }

    /** Sérialise un corps de requête en JSON. */
    private String json(Object valeur) throws Exception {
        return jsonMapper.writeValueAsString(valeur);
    }
}

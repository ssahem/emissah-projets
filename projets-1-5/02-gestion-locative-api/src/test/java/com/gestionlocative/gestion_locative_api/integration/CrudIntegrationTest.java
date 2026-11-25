package com.gestionlocative.gestion_locative_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestionlocative.gestion_locative_api.service.BailClotureService;
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests d'intégration HTTP des fonctionnalités F1 à F19.
 *
 * COUCHE       : Service
 * RÔLE         : Tests d'intégration HTTP des fonctionnalités F1 à F19.
 * TYPE         : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE    : migrations V1 à V8 sur base vierge, horloge fixe au 15 juin 2026,
 *              parcours complets via MockMvc.
 * CAS COUVERTS : création et enchaînement des cinq ressources, consultation (F6–F10),
 *                cycle de vie (F13–F16), restitution (F17–F18), revenus (F19)
 *                et traçabilité dans historique_bail.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(CrudIntegrationTest.HorlogeFixe.class)
class CrudIntegrationTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("gestion_locative")
            .withUsername("root")
            .withPassword("root");

    /** Remplace l'horloge système pour rendre les règles temporelles déterministes. */
    @TestConfiguration
    static class HorlogeFixe {

        /** Fournit un 15 juin 2026 fixe. */
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

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

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ── nettoyer() ───────────────────────────────────────────────────────────

    /**
     * Vide les tables utilisées par les scénarios d’intégration.
     */

    @Autowired private BailClotureService bailClotureService;

    @BeforeEach
    void nettoyer() {
        jdbcTemplate.execute("TRUNCATE TABLE bail_locataire, revision_loyer, bail, constat, "
                + "etat_lieux, regularisation, paiement, quittance, charge_immeuble, "
                + "appartement, immeuble, locataire, proprietaire, piece, historique_bail "
                + "RESTART IDENTITY CASCADE");
    }

    // ── Parcours nominal ─────────────────────────────────────────────────────

    // ── parcoursComplet_renvoie201Puis200() ──────────────────────────────────

    @Test
    @DisplayName("Le parcours complet crée les cinq ressources et les consulte")
    void parcoursComplet_renvoie201Puis200() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idAppartement = creerAppartement(idImmeuble, "1A");
        Integer idLocataire = creerLocataire("kofi");

        // ── Action ───────────────────────────────────────────────────────────
        MvcResult bail = creerBail(idAppartement, idLocataire, "2026-07-01", "2027-06-30");

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(bail.getResponse().getStatus()).isEqualTo(201);
        Integer idBail = id(bail, "idBail");
        assertThat(id(bail, "idLocataire")).isEqualTo(idLocataire);
        assertThat(lire(bail, "statut")).isEqualTo("EN_COURS");

        mockMvc.perform(get("/gestion-locative/proprietaire/list"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nom").value("Diallo"));
        mockMvc.perform(get("/gestion-locative/immeuble/get/" + idImmeuble))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nbAppartements").value(1));
        mockMvc.perform(get("/gestion-locative/appartement/list"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].numero").value("1A"));
        mockMvc.perform(get("/gestion-locative/locataire/list"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nom").value("Traore"));
        mockMvc.perform(get("/gestion-locative/bail/get/" + idBail))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nomLocataire").value("Traore"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM revision_loyer WHERE id_bail = " + idBail, Integer.class))
                .isEqualTo(1);
    }

    // ── creerBail_renvoie409QuandChevauchement() ─────────────────────────────

    @Test
    @DisplayName("Un bail chevauchant est refusé avec un 409")
    void creerBail_renvoie409QuandChevauchement() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartementLibre();

        // ── Action ───────────────────────────────────────────────────────────
        creerBail(idAppartement, 1, "2026-07-01", "2027-06-30");

        // ── Vérification ─────────────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idAppartement", idAppartement, "idLocataire", 1,
                                "dateDebut", "2027-01-01", "dateFin", "2027-12-31",
                                "loyerMensuel", 850.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Période déjà occupée"))
                .andExpect(jsonPath("$.idBailBloquant").isNumber());
    }

    // ── creerBail_renvoie400QuandDateFinInvalide() ───────────────────────────

    @Test
    @DisplayName("Une date de fin trop proche est refusée avec un 400")
    void creerBail_renvoie400QuandDateFinInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartementLibre();

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idAppartement", idAppartement, "idLocataire", 1,
                                "dateDebut", "2026-07-01", "dateFin", "2026-07-15",
                                "loyerMensuel", 850.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Date de fin invalide"));
    }

    // ── getById_renvoie404QuandInconnu() ─────────────────────────────────────

    @Test
    @DisplayName("Un identifiant inconnu renvoie 404")
    void getById_renvoie404QuandInconnu() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/bail/get/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Bail introuvable"));
    }

    // ── supprimerProprietaire_renvoie409QuandImmeuble() ──────────────────────

    @Test
    @DisplayName("Un propriétaire avec immeuble ne peut pas être supprimé")
    void supprimerProprietaire_renvoie409QuandImmeuble() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        creerImmeuble(idProprietaire);

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(delete("/gestion-locative/proprietaire/delete/" + idProprietaire))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Propriétaire non supprimable"));
    }

    // ── modifierBail_renvoie409QuandLoyerApresDebut() ────────────────────────

    @Test
    @DisplayName("Le loyer d'un bail commencé ne peut plus être modifié")
    void modifierBail_renvoie409QuandLoyerApresDebut() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartementLibre();
        MvcResult bail = creerBail(idAppartement, 1, "2026-06-01", "2027-05-31");
        Integer idBail = id(bail, "idBail");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(put("/gestion-locative/bail/update/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loyerMensuel\":900.00}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Modification interdite"));
    }

    // ── supprimerBailFutur_puisRecreer() ─────────────────────────────────────

    @Test
    @DisplayName("Un bail futur peut être supprimé puis l'appartement se libère")
    void supprimerBailFutur_puisRecreer() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartementLibre();
        Integer idBail = id(creerBail(idAppartement, 1, "2026-07-01", "2027-06-30"), "idBail");

        // ── Action ───────────────────────────────────────────────────────────
        mockMvc.perform(delete("/gestion-locative/bail/delete/" + idBail))
                .andExpect(status().isNoContent());

        // ── Vérification ─────────────────────────────────────────────────────
        creerBail(idAppartement, 1, "2026-07-01", "2027-06-30");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bail WHERE id_appartement = " + idAppartement, Integer.class))
                .isEqualTo(1);
    }

    // ── Consultation et recherche (F6–F10) ───────────────────────────────────

    // ── listerAppartements_renvoieOccupation() ───────────────────────────────

    @Test
    @DisplayName("F6 — les appartements portent leur état d'occupation")
    void listerAppartements_renvoieOccupation() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idApp1 = creerAppartement(idImmeuble, "1A", 45.50, 1);
        creerAppartement(idImmeuble, "2A", 60.00, 2);
        Integer idLocataire = creerLocataire("kofi");
        creerBail(idApp1, idLocataire, "2026-06-01", "2027-05-31");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/immeuble/appartements/" + idImmeuble))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statutOccupation").value("LOUE"))
                .andExpect(jsonPath("$[0].occupe").value(true))
                .andExpect(jsonPath("$[1].statutOccupation").value("LIBRE"));
    }

    // ── listerImmeubles_renvoieCompteurs() ───────────────────────────────────

    @Test
    @DisplayName("F7 — les immeubles d'un propriétaire portent leurs compteurs")
    void listerImmeubles_renvoieCompteurs() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idAppartement = creerAppartement(idImmeuble, "1A");
        Integer idLocataire = creerLocataire("kofi");
        creerBail(idAppartement, idLocataire, "2026-06-01", "2027-05-31");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/proprietaire/immeubles/" + idProprietaire))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nbAppartements").value(1))
                .andExpect(jsonPath("$[0].nbBauxEnCours").value(1));
    }

    // ── historiqueBaux_renvoieBailEnCours() ──────────────────────────────────

    @Test
    @DisplayName("F8 — l'historique d'un appartement signale le bail en cours")
    void historiqueBaux_renvoieBailEnCours() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idAppartement = creerAppartement(idImmeuble, "1A");
        Integer idLocataire = creerLocataire("kofi");
        creerBail(idAppartement, idLocataire, "2026-06-01", "2027-05-31");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/baux/" + idAppartement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomLocataire").value("Traore"))
                .andExpect(jsonPath("$[0].enCours").value(true))
                .andExpect(jsonPath("$[0].loyersPasses").isEmpty());
    }

    // ── rechercherAppartements_filtreBornes() ────────────────────────────────

    @Test
    @DisplayName("F9 — la recherche filtre les appartements et refuse des bornes inversées")
    void rechercherAppartements_filtreBornes() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        creerAppartement(idImmeuble, "1A", 40.00, 1);
        creerAppartement(idImmeuble, "2A", 60.00, 2);

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/list").param("surfaceMin", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numero").value("2A"));
        mockMvc.perform(get("/gestion-locative/appartement/list").param("pieces", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/gestion-locative/appartement/list")
                        .param("surfaceMin", "90").param("surfaceMax", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bornes de surface invalides"));
    }

    // ── listerBaux_renvoiePageResponse() ─────────────────────────────────────

    @Test
    @DisplayName("F10 — la liste des baux est paginée et la taille plafonnée à 100")
    void listerBaux_renvoiePageResponse() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idApp1 = creerAppartement(idImmeuble, "1A");
        Integer idApp2 = creerAppartement(idImmeuble, "2A", 60.00, 2);
        Integer idLocataire = creerLocataire("kofi");
        creerBail(idApp1, idLocataire, "2026-06-01", "2027-05-31");
        creerBail(idApp2, idLocataire, "2026-07-01", "2027-06-30");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/bail/list")
                        .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
        mockMvc.perform(get("/gestion-locative/bail/list").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    // ── Cycle de vie du bail (F13–F16) ───────────────────────────────────────

    // ── resilierBail_metAJourEtTrace() ───────────────────────────────────────

    @Test
    @DisplayName("F13 — la résiliation raccourcit le bail et alimente l'historique")
    void resilierBail_metAJourEtTrace() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartement();
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire, "2026-06-01", "2027-05-31"),
                "idBail");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/resiliation/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("dateEffet", "2026-09-30"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESILIE"))
                .andExpect(jsonPath("$.dateFin").value("2026-09-30"))
                .andExpect(jsonPath("$.dateResiliation").value("2026-09-30"));

        assertThat(nombreEvenements(idBail, "RESILIATION")).isEqualTo(1);
    }

    // ── renouvelerBail_prolongeEtTrace() ─────────────────────────────────────

    @Test
    @DisplayName("F14 — le renouvellement prolonge le bail et conserve l'ancienne fin")
    void renouvelerBail_prolongeEtTrace() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartement();
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire, "2026-06-01", "2026-12-31"),
                "idBail");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/renouvellement/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("dateFin", "2027-12-31"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.dateFin").value("2027-12-31"));

        assertThat(nombreEvenements(idBail, "RENOUVELLEMENT")).isEqualTo(1);
    }

    // ── reviserLoyer_conserveHistorique() ────────────────────────────────────

    @Test
    @DisplayName("F15 — la révision conserve l'ancien loyer et met à jour le courant")
    void reviserLoyer_conserveHistorique() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartement();
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire, "2026-06-01", "2027-05-31"),
                "idBail");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/revision/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "montant", 920.00, "dateEffet", "2027-01-01"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loyerMensuel").value(920.00));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM revision_loyer WHERE id_bail = ?",
                Integer.class, idBail)).isEqualTo(2);
    }

    // ── cloturerBauxEchus_metATermeEtTrace() ─────────────────────────────────

    @Test
    @DisplayName("F16 — la tâche clôture un bail échu et trace l'événement")
    void cloturerBauxEchus_metATermeEtTrace() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartement();
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire, "2026-05-01", "2026-06-01"),
                "idBail");

        // ── Action ───────────────────────────────────────────────────────────
        int nombre = bailClotureService.cloturerBauxEchus();

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(nombre).isEqualTo(1);
        mockMvc.perform(get("/gestion-locative/bail/get/" + idBail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"))
                .andExpect(jsonPath("$.dateCloture").value("2026-06-15"));
        assertThat(nombreEvenements(idBail, "CLOTURE")).isEqualTo(1);
    }

    // ── Restitution et revenus (F17–F19) ─────────────────────────────────────

    // ── listerAppartementsLibres_groupeParImmeuble() ─────────────────────────

    @Test
    @DisplayName("F17 — les appartements libres sont groupés par immeuble")
    void listerAppartementsLibres_groupeParImmeuble() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idOccupee = creerAppartement(idImmeuble, "1A");
        Integer idLibre = creerAppartement(idImmeuble, "2A", 60.00, 2);
        Integer idLocataire = creerLocataire("kofi");
        creerBail(idOccupee, idLocataire, "2026-07-01", "2027-06-30");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/libres")
                        .param("date", "2026-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idImmeuble").value(idImmeuble))
                .andExpect(jsonPath("$[0].appartements.length()").value(1))
                .andExpect(jsonPath("$[0].appartements[0].idAppartement").value(idLibre))
                .andExpect(jsonPath("$[0].appartements[0].dernierLoyer").value((Object) null));

        mockMvc.perform(get("/gestion-locative/appartement/libres")
                        .param("date", "01/12/2026"))
                .andExpect(status().isBadRequest());
    }

    // ── reconstituerOccupation_decoupePaliersEtVacances() ────────────────────

    @Test
    @DisplayName("F18 — l'occupation alterne paliers de loyer et vacance")
    void reconstituerOccupation_decoupePaliersEtVacances() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idAppartement = preparerAppartement();
        jdbcTemplate.update("UPDATE appartement SET date_creation = DATE '2026-01-01' "
                + "WHERE id_appartement = ?", idAppartement);
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire,
                "2026-01-01", "2026-03-31"), "idBail");
        mockMvc.perform(post("/gestion-locative/bail/revision/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "montant", 920.00, "dateEffet", "2026-02-01"))))
                .andExpect(status().isOk());

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/occupation/" + idAppartement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("LOUE"))
                .andExpect(jsonPath("$[0].loyerMensuel").value(850.00))
                .andExpect(jsonPath("$[0].fin").value("2026-01-31"))
                .andExpect(jsonPath("$[1].loyerMensuel").value(920.00))
                .andExpect(jsonPath("$[1].fin").value("2026-03-31"))
                .andExpect(jsonPath("$[2].type").value("VACANT"))
                .andExpect(jsonPath("$[2].debut").value("2026-04-01"))
                .andExpect(jsonPath("$[2].fin").value("2026-06-15"));

        mockMvc.perform(get("/gestion-locative/appartement/occupation/999"))
                .andExpect(status().isNotFound());
    }

    // ── reconstituerOccupation_jamaisLoue_depuisCreation() ───────────────────

    @Test
    @DisplayName("F18 — un logement jamais loué est vacant depuis sa création (V8)")
    void reconstituerOccupation_jamaisLoue_depuisCreation() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idAppartement = creerAppartement(idImmeuble, "1A");
        jdbcTemplate.update("UPDATE appartement SET date_creation = DATE '2026-03-01' "
                + "WHERE id_appartement = ?", idAppartement);

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/occupation/" + idAppartement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("VACANT"))
                .andExpect(jsonPath("$[0].debut").value("2026-03-01"))
                .andExpect(jsonPath("$[0].fin").doesNotExist());
    }

    // ── calculerRevenu_prorataEtRevisions() ──────────────────────────────────

    @Test
    @DisplayName("F19 — le revenu mensuel applique le prorata et les révisions")
    void calculerRevenu_prorataEtRevisions() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        Integer idAppartement = creerAppartement(idImmeuble, "1A");
        Integer idLocataire = creerLocataire("kofi");
        Integer idBail = id(creerBail(idAppartement, idLocataire,
                "2026-01-01", "2026-11-15"), "idBail");
        mockMvc.perform(post("/gestion-locative/bail/revision/" + idBail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "montant", 1000.00, "dateEffet", "2026-11-10"))))
                .andExpect(status().isOk());

        // ── Action + Vérification : 9 j à 850 + 6 j à 1000 = 455 sur 30 j ────
        mockMvc.perform(get("/gestion-locative/immeuble/revenu/" + idImmeuble)
                        .param("mois", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mois").value("2026-11"))
                .andExpect(jsonPath("$.detail.length()").value(1))
                .andExpect(jsonPath("$.detail[0].joursOccupes").value(15))
                .andExpect(jsonPath("$.detail[0].revenu").value(455.00))
                .andExpect(jsonPath("$.detail[0].tauxOccupation").value(50.00))
                .andExpect(jsonPath("$.revenuTotal").value(455.00))
                .andExpect(jsonPath("$.tauxOccupation").value(50.00));

        mockMvc.perform(get("/gestion-locative/immeuble/revenu/" + idImmeuble)
                        .param("mois", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Mois invalide"));
    }

    // ── Outils de préparation ────────────────────────────────────────────────


    // ── preparerAppartement() ────────────────────────────────────────────────

    /**
     * Crée un propriétaire, un immeuble et un appartement pour les tests de
     * cycle de vie.
     */

    private Integer preparerAppartement() throws Exception {
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        return creerAppartement(idImmeuble, "1A");
    }


    // ── nombreEvenements() ───────────────────────────────────────────────────

    /**
     * Compte les événements d'un motif donné pour un bail.
     */

    private int nombreEvenements(Integer idBail, String motif) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM historique_bail WHERE id_bail = ? AND motif = ?",
                Integer.class, idBail, motif);
    }


    // ── creerProprietaire() ──────────────────────────────────────────────────

    /**
     * Crée un propriétaire et retourne son identifiant.
     */

    private Integer creerProprietaire(String prefixe) throws Exception {
        MvcResult resultat = mockMvc.perform(post("/gestion-locative/proprietaire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Diallo", "prenom", "Awa",
                                "email", prefixe + ".diallo@example.test", "telephone", "0102030405"))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(resultat, "idProprietaire");
    }


    // ── creerImmeuble() ──────────────────────────────────────────────────────

    /**
     * Crée un immeuble et retourne son identifiant.
     */

    private Integer creerImmeuble(Integer idProprietaire) throws Exception {
        MvcResult resultat = mockMvc.perform(post("/gestion-locative/immeuble/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "adresse", "12 rue des Acacias", "codePostal", "75001",
                                "ville", "Paris", "idProprietaire", idProprietaire, "nbEtages", 3))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(resultat, "idImmeuble");
    }


    // ── creerAppartement() ───────────────────────────────────────────────────

    /**
     * Crée un appartement et retourne son identifiant.
     */

    private Integer creerAppartement(Integer idImmeuble, String numero) throws Exception {
        return creerAppartement(idImmeuble, numero, 45.50, 1);
    }


    // ── creerAppartement() ───────────────────────────────────────────────────

    /**
     * Crée un appartement avec une surface et un étage choisis.
     */

    private Integer creerAppartement(Integer idImmeuble, String numero,
                                     double surface, int etage) throws Exception {
        MvcResult resultat = mockMvc.perform(post("/gestion-locative/appartement/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "numero", numero, "surface", surface, "nbPieces", 2,
                                "etage", etage, "idImmeuble", idImmeuble))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(resultat, "idAppartement");
    }


    // ── creerLocataire() ─────────────────────────────────────────────────────

    /**
     * Crée un locataire et retourne son identifiant.
     */

    private Integer creerLocataire(String prefixe) throws Exception {
        MvcResult resultat = mockMvc.perform(post("/gestion-locative/locataire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Traore", "prenom", "Kofi",
                                "email", prefixe + ".traore@example.test", "telephone", "0102030406"))))
                .andExpect(status().isCreated())
                .andReturn();
        return id(resultat, "idLocataire");
    }


    // ── preparerAppartementLibre() ───────────────────────────────────────────

    /**
     * Prépare un appartement libre avec son locataire (identifiant 1).
     */

    private Integer preparerAppartementLibre() throws Exception {
        Integer idProprietaire = creerProprietaire("awa");
        Integer idImmeuble = creerImmeuble(idProprietaire);
        creerLocataire("kofi");
        return creerAppartement(idImmeuble, "1A");
    }


    // ── creerBail() ──────────────────────────────────────────────────────────

    /**
     * Crée un bail et retourne la réponse HTTP.
     */

    private MvcResult creerBail(Integer idAppartement, Integer idLocataire,
                                String debut, String fin) throws Exception {
        return mockMvc.perform(post("/gestion-locative/bail/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fin == null
                                ? Map.of("idAppartement", idAppartement, "idLocataire", idLocataire,
                                        "dateDebut", debut, "loyerMensuel", 850.00)
                                : Map.of("idAppartement", idAppartement, "idLocataire", idLocataire,
                                        "dateDebut", debut, "dateFin", fin, "loyerMensuel", 850.00))))
                .andReturn();
    }


    // ── id() ─────────────────────────────────────────────────────────────────

    /**
     * Lit un identifiant dans une réponse JSON.
     */

    private Integer id(MvcResult resultat, String champ) throws Exception {
        return Integer.valueOf(lire(resultat, champ));
    }


    // ── lire() ───────────────────────────────────────────────────────────────

    /**
     * Lit un champ texte dans une réponse JSON.
     */

    private String lire(MvcResult resultat, String champ) throws Exception {
        return objectMapper.readTree(resultat.getResponse().getContentAsString()).get(champ).asText();
    }
}

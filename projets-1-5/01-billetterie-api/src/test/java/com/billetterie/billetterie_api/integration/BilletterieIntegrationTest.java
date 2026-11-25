package com.billetterie.billetterie_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Client;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Tarif;
import com.billetterie.billetterie_api.repository.CategorieRepository;
import com.billetterie.billetterie_api.repository.ClientRepository;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import com.billetterie.billetterie_api.repository.StatutRepository;
import com.billetterie.billetterie_api.repository.TarifRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests d'intégration de bout en bout.
 *
 * COUCHE       : Service
 * RÔLE         : Tests d'intégration de bout en bout.
 * TYPE         : Intégration (Testcontainers PostgreSQL)
 * STRATÉGIE    : schéma issu des migrations Flyway V1 à V6, API via MockMvc, horloge gelée au
 *              23 novembre 2026 10:00 UTC. Chaque test est transactionnel et annulé à la fin :
 *              les fixtures ne se chevauchent jamais.
 * CAS COUVERTS : F1 à F19 (scénario complet puis cas ciblés sur les règles limites), arbitrage E-04.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BilletterieIntegrationTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDateTime MAINTENANT = LocalDateTime.parse("2026-11-23T10:00:00");

    @Container
    static PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("billetterie")
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

    /** Remplace l'horloge système pour rendre F14 et F15 déterministes. */
    @TestConfiguration
    static class HorlogeFixeConfiguration {

        @Bean
        @Primary
        Clock clockFixe() {
            return Clock.fixed(MAINTENANT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;
    @Autowired private SalleRepository salleRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private EvenementRepository evenementRepository;
    @Autowired private TarifRepository tarifRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationPlaceRepository reservationPlaceRepository;
    @Autowired private StatutRepository statutRepository;

    // ── fluxComplet_salleEvenementReservationPaiementEtRestitutions() ────────

    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("Le flux complet de la salle au paiement respecte les règles métier")
    void fluxComplet_salleEvenementReservationPaiementEtRestitutions() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        entityManager.persist(new Categorie("ORCHESTRE"));
        entityManager.flush();
        String salleJson = """
                {"nom":"Grand Hall","adresse":"12 rue des Arts","ville":"Abidjan","capacite":120}
                """;
        String evenementJson = """
                {"titre":"Concert Jazz","type":"CONCERT","placementNumerote":true,
                 "dateHeure":"%s","idSalle":%%d}
                """.formatted(MAINTENANT.plusDays(30));
        String reservationJson = """
                {"nom":"Dupont","prenom":"Jean","email":"jean@example.com",
                 "idEvenement":%d,"rang":"A","numero":12}
                """;

        // ── Action + Vérification — F1 ───────────────────────────────────────
        MvcResult salleResult = mockMvc.perform(post("/billetterie/salle/create")
                        .contentType(MediaType.APPLICATION_JSON).content(salleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ville").value("Abidjan"))
                .andReturn();
        int idSalle = json(salleResult).get("idSalle").asInt();

        Salle salle = salleRepository.findById(idSalle).orElseThrow();
        Categorie categorie = categorieRepository.findByLibelleIgnoreCase("ORCHESTRE").orElseThrow();
        placeRepository.saveAndFlush(new Place("A", 12, salle, categorie));

        // ── Action + Vérification — F3 ───────────────────────────────────────
        MvcResult evenementResult = mockMvc.perform(post("/billetterie/evenement/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evenementJson.formatted(idSalle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Concert Jazz"))
                .andReturn();
        int idEvenement = json(evenementResult).get("idEvenement").asInt();
        Evenement evenement = evenementRepository.findById(idEvenement).orElseThrow();
        entityManager.persist(new Tarif(evenement, "ORCHESTRE", new BigDecimal("25.00")));
        entityManager.flush();

        // ── Action + Vérification — F4 et F11 ────────────────────────────────
        String corpsReservation = reservationJson.formatted(idEvenement);
        MvcResult reservationResult = mockMvc.perform(post("/billetterie/reservation/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corpsReservation))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("PROVISOIRE"))
                .andReturn();
        int idReservation = json(reservationResult).get("idReservation").asInt();

        mockMvc.perform(post("/billetterie/reservation/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corpsReservation))
                .andExpect(status().isConflict());

        // ── Action + Vérification — F5, F6 et F7 ─────────────────────────────
        mockMvc.perform(get("/billetterie/salle/places/{id}", idSalle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(12));
        mockMvc.perform(get("/billetterie/salle/places/{id}", idSalle)
                        .param("categorie", "BALCON"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Catégorie inconnue"));
        mockMvc.perform(get("/billetterie/salle/evenements/{id}", idSalle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avenir").value(true));

        // ── Action + Vérification — F9 et F10 ────────────────────────────────
        mockMvc.perform(get("/billetterie/reservation/list").param("page", "0").param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/billetterie/reservation/list").param("email", "JEAN@EXAMPLE.COM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReservation").value(idReservation));

        // ── Action + Vérification — F14 ──────────────────────────────────────
        mockMvc.perform(post("/billetterie/reservation/paiement/{id}", idReservation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PAYEE"));

        // ── Action + Vérification — F17 et F19 ───────────────────────────────
        mockMvc.perform(get("/billetterie/evenement/places-libres/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placesTotales").value(1))
                .andExpect(jsonPath("$.parCategorie[0].placesLibres").value(0));
        mockMvc.perform(get("/billetterie/evenement/reservations/export/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString(
                        "rang,numero,categorie,nom,email,statut,date_creation")));

        // ── Vérification finale ──────────────────────────────────────────────
        assertThat(salleRepository.count()).isEqualTo(1);
        assertThat(evenementRepository.count()).isEqualTo(1);
    }

    // ── place_crudCreateUpdateDelete() ───────────────────────────────────────

    @Test
    @DisplayName("F2 — les places se créent, se modifient et se suppriment via l'API")
    void place_crudCreateUpdateDelete() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        creerCategorie("BALCON");
        int idSalle = creerSalle("Abidjan", 2);

        // ── Action + Vérification ────────────────────────────────────────────
        int idPlace = creerPlace(idSalle, "A", 1, "ORCHESTRE");

        mockMvc.perform(post("/billetterie/place/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idSalle":%d,"rang":"A","numero":1,"categorie":"ORCHESTRE"}
                                """.formatted(idSalle)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Place déjà existante"));

        creerPlace(idSalle, "B", 1, "ORCHESTRE");
        mockMvc.perform(post("/billetterie/place/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idSalle":%d,"rang":"C","numero":1,"categorie":"ORCHESTRE"}
                                """.formatted(idSalle)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Capacité insuffisante"));

        mockMvc.perform(get("/billetterie/place/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rang").value("A"));
        mockMvc.perform(get("/billetterie/place/get/{id}", idPlace))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(1));

        mockMvc.perform(put("/billetterie/place/update/{id}", idPlace)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idSalle":%d,"rang":"A","numero":2,"categorie":"BALCON"}
                                """.formatted(idSalle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorie").value("BALCON"));
        mockMvc.perform(get("/billetterie/place/get/{id}", idPlace))
                .andExpect(jsonPath("$.numero").value(2));

        mockMvc.perform(delete("/billetterie/place/delete/{id}", idPlace))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/billetterie/place/get/{id}", idPlace))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Place introuvable"));
    }

    // ── evenement_triEtFiltrePeriode() ───────────────────────────────────────

    @Test
    @DisplayName("F3 et F8 — les événements sont triés et filtrés par période, bornes incluses")
    void evenement_triEtFiltrePeriode() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalle = creerSalle("Abidjan", 1);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        LocalDateTime debut = MAINTENANT.plusDays(10);
        LocalDateTime milieu = MAINTENANT.plusDays(20);
        LocalDateTime fin = MAINTENANT.plusDays(30);
        creerEvenement(idSalle, "Concert milieu", milieu);
        creerEvenement(idSalle, "Concert début", debut);
        creerEvenement(idSalle, "Concert fin", fin);

        // ── Action + Vérification — F3 ───────────────────────────────────────
        mockMvc.perform(get("/billetterie/evenement/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Concert début"))
                .andExpect(jsonPath("$[2].titre").value("Concert fin"));

        // ── Action + Vérification — F8 ───────────────────────────────────────
        mockMvc.perform(get("/billetterie/evenement/list")
                        .param("du", debut.toString()).param("au", milieu.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titre").value("Concert début"))
                .andExpect(jsonPath("$[1].titre").value("Concert milieu"));
        mockMvc.perform(get("/billetterie/evenement/list").param("du", milieu.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/billetterie/evenement/list").param("au", milieu.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/billetterie/evenement/list")
                        .param("du", fin.toString()).param("au", debut.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Période invalide"));
    }

    // ── salleSansDonnees_restitutionsVides() ─────────────────────────────────

    @Test
    @DisplayName("F5, F7 et F17 — une salle et un événement sans réservation renvoient des listes vides")
    void salleSansDonnees_restitutionsVides() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalleVide = creerSalle("Bouaké", 4);
        int idSalle = creerSalle("Abidjan", 4);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        int idEvenement = creerEvenement(idSalle, "Concert sans réservation", MAINTENANT.plusDays(12));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/billetterie/salle/places/{id}", idSalleVide))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/billetterie/salle/evenements/{id}", idSalleVide))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/billetterie/evenement/places-libres/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placesTotales").value(1))
                .andExpect(jsonPath("$.parCategorie[0].placesLibres").value(1))
                .andExpect(jsonPath("$.parCategorie[0].placesOccupees").value(0));
    }

    // ── salle_erreursEtSuppression() ─────────────────────────────────────────

    @Test
    @DisplayName("F1 — validation 400, salle introuvable 404 et suppression 204")
    void salle_erreursEtSuppression() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/billetterie/salle/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Salle invalide","adresse":"1 rue de Test","ville":"Abidjan","capacite":0}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/billetterie/salle/get/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Salle introuvable"));

        int idSalle = creerSalle("Yamoussoukro", 10);
        mockMvc.perform(delete("/billetterie/salle/delete/{id}", idSalle))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/billetterie/salle/get/{id}", idSalle))
                .andExpect(status().isNotFound());
    }

    // ── annulation_remboursementRemplissageEtExport() ────────────────────────

    @Test
    @DisplayName("F15, F18 et F19 — annulation, remplissage recalculé et export filtré")
    void annulation_remboursementRemplissageEtExport() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalle = creerSalle("Abidjan", 2);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        creerPlace(idSalle, "A", 2, "ORCHESTRE");
        int idEvenement = creerEvenement(idSalle, "Concert annulation", MAINTENANT.plusDays(30));
        creerTarif(idEvenement, "ORCHESTRE", "25.00");
        String emailPayant = unique("payant") + "@example.com";
        String emailProvisoire = unique("provisoire") + "@example.com";
        int idPayee = creerReservation(idEvenement, "A", 1, emailPayant);
        creerReservation(idEvenement, "A", 2, emailProvisoire);
        mockMvc.perform(post("/billetterie/reservation/paiement/{id}", idPayee))
                .andExpect(status().isOk());

        // ── Action + Vérification — F18 avant annulation ─────────────────────
        mockMvc.perform(get("/billetterie/evenement/remplissage/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placesTotales").value(2))
                .andExpect(jsonPath("$.provisoires").value(1))
                .andExpect(jsonPath("$.payees").value(1))
                .andExpect(jsonPath("$.taux").value(100.00))
                .andExpect(jsonPath("$.parCategorie[0].categorie").value("ORCHESTRE"));

        // ── Action + Vérification — F15 ──────────────────────────────────────
        mockMvc.perform(post("/billetterie/reservation/annulation/{id}", idPayee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"))
                .andExpect(jsonPath("$.montantRembourse").value(25.00));
        mockMvc.perform(post("/billetterie/reservation/annulation/{id}", idPayee))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Annulation impossible"));
        mockMvc.perform(get("/billetterie/reservation/get/{id}", idPayee))
                .andExpect(jsonPath("$.statut").value("ANNULEE"))
                .andExpect(jsonPath("$.montantRembourse").value(25.00));

        // ── Action + Vérification — F18 et F17 après annulation ──────────────
        mockMvc.perform(get("/billetterie/evenement/remplissage/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provisoires").value(1))
                .andExpect(jsonPath("$.payees").value(0))
                .andExpect(jsonPath("$.taux").value(50.00));
        mockMvc.perform(get("/billetterie/evenement/places-libres/{id}", idEvenement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parCategorie[0].placesLibres").value(1))
                .andExpect(jsonPath("$.parCategorie[0].placesOccupees").value(1));

        // ── Action + Vérification — F19 ──────────────────────────────────────
        MvcResult sansAnnulees = mockMvc.perform(
                        get("/billetterie/evenement/reservations/export/{id}", idEvenement))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(sansAnnulees.getResponse().getContentAsString())
                .contains(emailProvisoire)
                .doesNotContain(emailPayant);
        MvcResult avecAnnulees = mockMvc.perform(get("/billetterie/evenement/reservations/export/{id}", idEvenement)
                        .param("inclureAnnulees", "true"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(avecAnnulees.getResponse().getContentAsString())
                .contains(emailProvisoire, emailPayant);
    }

    // ── paiement_apresDelaiExpire() ──────────────────────────────────────────

    @Test
    @DisplayName("F14 — le paiement est refusé après le délai de quinze minutes")
    void paiement_apresDelaiExpire() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalle = creerSalle("Abidjan", 1);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        int idEvenement = creerEvenement(idSalle, "Concert expiration", MAINTENANT.plusDays(20));
        creerTarif(idEvenement, "ORCHESTRE", "25.00");
        Evenement evenement = evenementRepository.findById(idEvenement).orElseThrow();
        Reservation expiree = enregistrerReservation(evenement, "A", 1,
                MAINTENANT.minusMinutes(16), unique("expire") + "@example.com");

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/billetterie/reservation/paiement/{id}", expiree.getIdReservation()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Réservation expirée"));
    }

    // ── reservationPlace_uniciteEnBase() ─────────────────────────────────────

    @Test
    @DisplayName("F11 — l'unicité (événement, position) est garantie par la base")
    void reservationPlace_uniciteEnBase() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalle = creerSalle("Abidjan", 1);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        int idEvenement = creerEvenement(idSalle, "Concert unicité", MAINTENANT.plusDays(20));
        Evenement evenement = evenementRepository.findById(idEvenement).orElseThrow();
        enregistrerReservation(evenement, "A", 1, MAINTENANT, unique("un") + "@example.com");

        Client secondClient = clientRepository.saveAndFlush(
                new Client("Martin", "Claire", unique("deux") + "@example.com"));
        Reservation seconde = new Reservation(MAINTENANT, secondClient, evenement);
        seconde.ajouterPlace(new ReservationPlace(seconde, "A", 1, idEvenement));
        seconde.ajouterHistorique(new HistoriqueStatut(seconde,
                statutRepository.findByLibelleIgnoreCase("PROVISOIRE").orElseThrow(), MAINTENANT));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> reservationRepository.saveAndFlush(seconde))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── reservationPlace_uniciteAvecPositionsNulles() ────────────────────────

    @Test
    @DisplayName("E-04 — l'unicité couvre aussi les positions rang/numero NULL (V6)")
    void reservationPlace_uniciteAvecPositionsNulles() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        creerCategorie("ORCHESTRE");
        int idSalle = creerSalle("Abidjan", 1);
        creerPlace(idSalle, "A", 1, "ORCHESTRE");
        int idEvenement = creerEvenement(idSalle, "Concert positions nulles", MAINTENANT.plusDays(20));
        Evenement evenement = evenementRepository.findById(idEvenement).orElseThrow();
        enregistrerReservation(evenement, null, null, MAINTENANT, unique("nul1") + "@example.com");

        Client secondClient = clientRepository.saveAndFlush(
                new Client("Martin", "Claire", unique("nul2") + "@example.com"));
        Reservation seconde = new Reservation(MAINTENANT, secondClient, evenement);
        seconde.ajouterPlace(new ReservationPlace(seconde, null, null, idEvenement));
        seconde.ajouterHistorique(new HistoriqueStatut(seconde,
                statutRepository.findByLibelleIgnoreCase("PROVISOIRE").orElseThrow(), MAINTENANT));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> reservationRepository.saveAndFlush(seconde))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── FICTIONS ─────────────────────────────────────────────────────────────

    /** Crée une catégorie de référence si elle n'existe pas déjà. */

    // ── creerCategorie() ─────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerCategorie().
     */

    private void creerCategorie(String libelle) {
        if (!categorieRepository.existsByLibelleIgnoreCase(libelle)) {
            entityManager.persist(new Categorie(libelle));
            entityManager.flush();
        }
    }

    /** Crée une salle via l'API F1 et retourne son identifiant. */

    // ── creerSalle() ─────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerSalle().
     */

    private int creerSalle(String ville, int capacite) throws Exception {
        String corps = """
                {"nom":"%s","adresse":"1 rue de Test","ville":"%s","capacite":%d}
                """.formatted(unique("Salle"), ville, capacite);
        MvcResult resultat = mockMvc.perform(post("/billetterie/salle/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andReturn();
        return json(resultat).get("idSalle").asInt();
    }

    /** Crée une place via l'API F2 et retourne son identifiant. */

    // ── creerPlace() ─────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerPlace().
     */

    private int creerPlace(int idSalle, String rang, int numero, String categorie) throws Exception {
        String corps = """
                {"idSalle":%d,"rang":"%s","numero":%d,"categorie":"%s"}
                """.formatted(idSalle, rang, numero, categorie);
        MvcResult resultat = mockMvc.perform(post("/billetterie/place/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andReturn();
        return json(resultat).get("idPlace").asInt();
    }

    /** Crée un événement via l'API F3 et retourne son identifiant. */

    // ── creerEvenement() ─────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerEvenement().
     */

    private int creerEvenement(int idSalle, String titre, LocalDateTime dateHeure) throws Exception {
        String corps = """
                {"titre":"%s","type":"CONCERT","placementNumerote":true,
                 "dateHeure":"%s","idSalle":%d}
                """.formatted(titre, dateHeure, idSalle);
        MvcResult resultat = mockMvc.perform(post("/billetterie/evenement/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andReturn();
        return json(resultat).get("idEvenement").asInt();
    }

    /** Crée le tarif d'une catégorie pour un événement. */

    // ── creerTarif() ─────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerTarif().
     */

    private void creerTarif(int idEvenement, String categorie, String prix) {
        entityManager.persist(new Tarif(
                evenementRepository.findById(idEvenement).orElseThrow(), categorie, new BigDecimal(prix)));
        entityManager.flush();
    }

    /** Crée une réservation via l'API F4 et retourne son identifiant. */

    // ── creerReservation() ───────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour creerReservation().
     */

    private int creerReservation(int idEvenement, String rang, int numero, String email) throws Exception {
        String corps = """
                {"nom":"Dupont","prenom":"Jean","email":"%s",
                 "idEvenement":%d,"rang":"%s","numero":%d}
                """.formatted(email, idEvenement, rang, numero);
        MvcResult resultat = mockMvc.perform(post("/billetterie/reservation/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andReturn();
        return json(resultat).get("idReservation").asInt();
    }

    /** Enregistre directement une réservation avec une date de création choisie. */

    // ── enregistrerReservation() ─────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour enregistrerReservation().
     */

    private Reservation enregistrerReservation(Evenement evenement, String rang, Integer numero,
                                               LocalDateTime dateCreation, String email) {
        Client client = clientRepository.saveAndFlush(new Client("Dupont", "Jean", email));
        Reservation reservation = new Reservation(dateCreation, client, evenement);
        reservation.ajouterPlace(new ReservationPlace(reservation, rang, numero, evenement.getIdEvenement()));
        reservation.ajouterHistorique(new HistoriqueStatut(reservation,
                statutRepository.findByLibelleIgnoreCase("PROVISOIRE").orElseThrow(), dateCreation));
        return reservationRepository.saveAndFlush(reservation);
    }

    /** Génère un suffixe court pour éviter toute collision de clé métier entre méthodes. */

    // ── unique() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour unique().
     */

    private static String unique(String prefixe) {
        return prefixe + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ── json() ───────────────────────────────────────────────────────────────

    /** Extrait le corps JSON d'une réponse MockMvc. */
    private JsonNode json(MvcResult resultat) throws Exception {
        return objectMapper.readTree(resultat.getResponse().getContentAsString());
    }
}

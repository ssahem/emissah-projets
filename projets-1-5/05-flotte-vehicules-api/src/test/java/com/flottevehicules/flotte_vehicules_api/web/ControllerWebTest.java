package com.flottevehicules.flotte_vehicules_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.CreateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.AlerteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.AlertesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.CoutVehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.DisponibilitesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.EntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.HistoriqueEntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.KilometrageSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.PageResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.SiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeSiteResponse;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.GlobalExceptionHandler;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import com.flottevehicules.flotte_vehicules_api.service.AffectationService;
import com.flottevehicules.flotte_vehicules_api.service.AlerteService;
import com.flottevehicules.flotte_vehicules_api.service.EntretienService;
import com.flottevehicules.flotte_vehicules_api.service.SalarieService;
import com.flottevehicules.flotte_vehicules_api.service.SiteService;
import com.flottevehicules.flotte_vehicules_api.service.VehiculeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Codes de réponse HTTP, validation et Problem Details de tous les endpoints.
 *
 * COUCHE       : Service
 * RÔLE         : Codes de réponse HTTP, validation et Problem Details de tous les endpoints.
 * SÉCURITÉ     : aucune ; aucun jeton n'est transmis dans les requêtes.
 * IMPORT       : GlobalExceptionHandler pour vérifier les RFC 9457.
 * TYPE         : WebMvcTest (couche web seule, services simulés).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ControllerWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;

    @MockitoBean private SiteService siteService;
    @MockitoBean private SalarieService salarieService;
    @MockitoBean private VehiculeService vehiculeService;
    @MockitoBean private AffectationService affectationService;
    @MockitoBean private EntretienService entretienService;
    @MockitoBean private AlerteService alerteService;

    // ── json() ───────────────────────────────────────────────────────────────

    /**
     * Sérialise le corps de requête en JSON.
     */

    @MockitoBean private VueRepository vueRepository;

    private String json(Object value) throws Exception {
        return jsonMapper.writeValueAsString(value);
    }

    // ── Sites ────────────────────────────────────────────────────────────────

    // ── createSiteReturns201() ───────────────────────────────────────────────

    @Test
    @DisplayName("POST site/create renvoie 201 et un en-tête Location")
    void createSiteReturns201() throws Exception {
        when(siteService.create(any(CreateSiteRequest.class)))
                .thenReturn(new SiteResponse(1, "Paris Centre", "Paris", "12 rue de la Paix"));

        mockMvc.perform(post("/flotte-vehicules/site/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nom", "Paris Centre", "ville", "Paris", "adresse", "12 rue de la Paix"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/flotte-vehicules/site/1"))
                .andExpect(jsonPath("$.ville").value("Paris"));
    }

    // ── listSitesReturns200() ────────────────────────────────────────────────

    @Test
    @DisplayName("GET site/list renvoie la liste triée")
    void listSitesReturns200() throws Exception {
        when(siteService.findAll()).thenReturn(List.of(
                new SiteResponse(2, "Lyon", "Lyon", "adresse"),
                new SiteResponse(1, "Paris", "Paris", "adresse")));

        mockMvc.perform(get("/flotte-vehicules/site/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ville").value("Lyon"));
    }

    // ── getSiteIntrouvableReturns404() ───────────────────────────────────────

    @Test
    @DisplayName("GET site/{id} inconnu renvoie 404 en Problem Details")
    void getSiteIntrouvableReturns404() throws Exception {
        when(siteService.findById(99)).thenThrow(new RessourceIntrouvableException("Site", 99));

        mockMvc.perform(get("/flotte-vehicules/site/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.type").value(
                        "https://api.flotte-vehicules.local/problems/ressource-introuvable"));
    }

    // ── updateSiteReturns200() ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT site/{id}/update renvoie le site modifié")
    void updateSiteReturns200() throws Exception {
        when(siteService.update(eq(1), any()))
                .thenReturn(new SiteResponse(1, "Paris Centre", "Lyon", "14 rue de la Paix"));

        mockMvc.perform(put("/flotte-vehicules/site/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ville", "Lyon", "adresse", "14 rue de la Paix"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ville").value("Lyon"));
    }

    // ── deleteSiteReturns204() ───────────────────────────────────────────────

    @Test
    @DisplayName("DELETE site/{id}/delete renvoie 204")
    void deleteSiteReturns204() throws Exception {
        doNothing().when(siteService).delete(1);

        mockMvc.perform(delete("/flotte-vehicules/site/1/delete"))
                .andExpect(status().isNoContent());
    }

    // ── vehiculesSiteReturns200() ────────────────────────────────────────────

    @Test
    @DisplayName("GET site/{id}/vehicules renvoie le parc du site")
    void vehiculesSiteReturns200() throws Exception {
        when(siteService.vehicules(1)).thenReturn(List.of(
                new VehiculeSiteResponse(1, "AB-123-CD", "Peugeot", "308", "LIBRE", 19000)));

        mockMvc.perform(get("/flotte-vehicules/site/1/vehicules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].etatAffectation").value("LIBRE"));
    }

    // ── kilometrageSiteReturns200() ──────────────────────────────────────────

    @Test
    @DisplayName("GET site/{id}/kilometrage renvoie le total et le détail")
    void kilometrageSiteReturns200() throws Exception {
        when(siteService.kilometrage(1, "2026-11")).thenReturn(new KilometrageSiteResponse(
                1, "Paris Centre", "2026-11", 27000, List.of()));

        mockMvc.perform(get("/flotte-vehicules/site/1/kilometrage").param("mois", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kmTotal").value(27000));
    }

    // ── Véhicules ────────────────────────────────────────────────────────────

    // ── createVehiculeReturns201() ───────────────────────────────────────────

    @Test
    @DisplayName("POST vehicule/create renvoie 201")
    void createVehiculeReturns201() throws Exception {
        when(vehiculeService.create(any(CreateVehiculeRequest.class)))
                .thenReturn(new VehiculeResponse(1, "AB-123-CD", "Peugeot", "308",
                        LocalDate.parse("2024-01-15"), "PROPRIETE", 1, "Paris Centre", 19000));

        mockMvc.perform(post("/flotte-vehicules/vehicule/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "immatriculation", "AB-123-CD", "marque", "Peugeot", "modele", "308",
                                "dateMiseEnService", "2024-01-15", "idSite", 1,
                                "modeAcquisition", "PROPRIETE", "prixAchat", 25000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomSite").value("Paris Centre"));
    }

    // ── listVehiculesReturns200() ────────────────────────────────────────────

    @Test
    @DisplayName("GET vehicule/list transmet les critères de recherche")
    void listVehiculesReturns200() throws Exception {
        when(vehiculeService.findAll("AB-123", "Peugeot")).thenReturn(List.of());

        mockMvc.perform(get("/flotte-vehicules/vehicule/list")
                        .param("immatriculation", "AB-123").param("marque", "Peugeot"))
                .andExpect(status().isOk());
        verify(vehiculeService).findAll("AB-123", "Peugeot");
    }

    // ── coutVehiculeReturns200() ─────────────────────────────────────────────

    @Test
    @DisplayName("GET vehicule/{id}/cout renvoie le coût et la répartition")
    void coutVehiculeReturns200() throws Exception {
        when(vehiculeService.cout(1)).thenReturn(new CoutVehiculeResponse(
                1, "AB-123-CD", BigDecimal.valueOf(600), new BigDecimal("0.03"),
                19000, 2, List.of()));

        mockMvc.perform(get("/flotte-vehicules/vehicule/1/cout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coutTotal").value(600));
    }

    // ── disponiblesReturns200() ──────────────────────────────────────────────

    @Test
    @DisplayName("GET vehicule/disponibles renvoie les groupes par site")
    void disponiblesReturns200() throws Exception {
        when(vehiculeService.disponibles(null, null)).thenReturn(List.of(
                new DisponibilitesParSiteResponse(1, "Paris Centre",
                        LocalDate.parse("2026-11-23"), LocalDate.parse("2026-11-29"), List.of())));

        mockMvc.perform(get("/flotte-vehicules/vehicule/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomSite").value("Paris Centre"));
    }

    // ── historiqueReturns200() ───────────────────────────────────────────────

    @Test
    @DisplayName("GET vehicule/{id}/entretiens renvoie l'historique F8")
    void historiqueReturns200() throws Exception {
        when(entretienService.historique(1)).thenReturn(new HistoriqueEntretienResponse(
                1, "AB-123-CD", BigDecimal.ZERO, List.of()));

        mockMvc.perform(get("/flotte-vehicules/vehicule/1/entretiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coutCumule").value(0));
    }

    // ── Salariés et affectations ─────────────────────────────────────────────

    // ── createSalarieConflictReturns409() ────────────────────────────────────

    @Test
    @DisplayName("POST salarie/create avec un matricule pris renvoie 409")
    void createSalarieConflictReturns409() throws Exception {
        when(salarieService.create(any())).thenThrow(new ConflitMetierException(
                "salarie-matricule-deja-utilise", "Matricule pris"));

        mockMvc.perform(post("/flotte-vehicules/salarie/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nom", "Martin", "prenom", "Alice",
                                "email", "alice@example.test", "matricule", "MAT001", "service", "Direction"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.flotte-vehicules.local/problems/salarie-matricule-deja-utilise"));
    }

    // ── affectationsSalarieReturns200() ──────────────────────────────────────

    @Test
    @DisplayName("GET salarie/{id}/affectations renvoie l'historique F7")
    void affectationsSalarieReturns200() throws Exception {
        when(salarieService.affectations(1)).thenReturn(List.of());

        mockMvc.perform(get("/flotte-vehicules/salarie/1/affectations"))
                .andExpect(status().isOk());
    }

    // ── createAffectationReturns201() ────────────────────────────────────────

    @Test
    @DisplayName("POST affectation/create renvoie 201")
    void createAffectationReturns201() throws Exception {
        when(affectationService.create(any(CreateAffectationRequest.class)))
                .thenReturn(new AffectationResponse(1, LocalDate.parse("2026-11-01"), null,
                        1, "AB-123-CD", 1, "Alice Martin", null, true));

        mockMvc.perform(post("/flotte-vehicules/affectation/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("idVehicule", 1, "idSalarie", 1, "dateDebut", "2026-11-01"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enCours").value(true));
    }

    // ── restitutionInvalideReturns400() ──────────────────────────────────────

    @Test
    @DisplayName("POST affectation/{id}/restitution avec une date invalide renvoie 400")
    void restitutionInvalideReturns400() throws Exception {
        when(affectationService.restituer(eq(1), any()))
                .thenThrow(new DonneeInvalideException("restitution-date-invalide", "Date invalide"));

        mockMvc.perform(post("/flotte-vehicules/affectation/1/restitution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("date", "2026-10-01", "km", 19000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Donnée invalide"));
    }

    // ── Entretiens ───────────────────────────────────────────────────────────

    // ── listEntretiensReturns200() ───────────────────────────────────────────

    @Test
    @DisplayName("GET entretien/list renvoie une page")
    void listEntretiensReturns200() throws Exception {
        when(entretienService.findAll(0, 20, "date,desc")).thenReturn(
                new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/flotte-vehicules/entretien/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20));
    }

    // ── getEntretienIntrouvableReturns404() ──────────────────────────────────

    @Test
    @DisplayName("GET entretien/{id} inconnu renvoie 404")
    void getEntretienIntrouvableReturns404() throws Exception {
        when(entretienService.findById(99))
                .thenThrow(new RessourceIntrouvableException("Entretien", 99));

        mockMvc.perform(get("/flotte-vehicules/entretien/99"))
                .andExpect(status().isNotFound());
    }

    // ── createEntretienInvalideReturns400() ──────────────────────────────────

    @Test
    @DisplayName("POST entretien/create avec un coût nul renvoie 400 et les champs")
    void createEntretienInvalideReturns400() throws Exception {
        mockMvc.perform(post("/flotte-vehicules/entretien/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-11-10\",\"idVehicule\":1,"
                                + "\"idTypeEntretien\":1,\"idPrestataire\":1,\"cout\":0,\"km\":19000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"))
                .andExpect(jsonPath("$.champs").isArray());
    }

    // ── Alertes, vues et validation générique ────────────────────────────────

    // ── listAlertesReturns200() ──────────────────────────────────────────────

    @Test
    @DisplayName("GET alerte/list renvoie les alertes persistées")
    void listAlertesReturns200() throws Exception {
        when(alerteService.lister(null)).thenReturn(List.of(new AlerteResponse(
                1, 2, "CD-456-EF", "Paris Centre", 2, "ECHEANCE_ENTRETIEN",
                "2026-11", LocalDate.parse("2026-11-24"), LocalDate.parse("2026-11-30"),
                500, 6, "Échéance proche")));

        mockMvc.perform(get("/flotte-vehicules/alerte/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motif").value("ECHEANCE_ENTRETIEN"));
    }

    // ── genererAlertesReturns200() ───────────────────────────────────────────

    @Test
    @DisplayName("POST alerte/generer renvoie les alertes par site")
    void genererAlertesReturns200() throws Exception {
        when(alerteService.genererAlertes()).thenReturn(List.of(
                new AlertesParSiteResponse(1, "Paris Centre", List.of())));

        mockMvc.perform(post("/flotte-vehicules/alerte/generer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomSite").value("Paris Centre"));
    }

    // ── lireVueReturns200() ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET vue/{nom} lit une vue autorisée")
    void lireVueReturns200() throws Exception {
        when(vueRepository.lireVue("v_bilan_vehicule"))
                .thenReturn(List.of(Map.of("id_vehicule", 1)));

        mockMvc.perform(get("/flotte-vehicules/vue/v_bilan_vehicule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_vehicule").value(1));
    }

    // ── lireVueInconnueReturns400() ──────────────────────────────────────────

    @Test
    @DisplayName("GET vue/{nom} avec un nom inconnu renvoie 400")
    void lireVueInconnueReturns400() throws Exception {
        when(vueRepository.lireVue("inconnue"))
                .thenThrow(new DonneeInvalideException("vue-inconnue", "Vue non autorisée"));

        mockMvc.perform(get("/flotte-vehicules/vue/inconnue"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/flotte-vehicules/site/create",
            "/flotte-vehicules/vehicule/create",
            "/flotte-vehicules/salarie/create",
            "/flotte-vehicules/affectation/create",
            "/flotte-vehicules/entretien/create"
    })

    // ── emptyBodyReturns400() ────────────────────────────────────────────────

    @DisplayName("Un corps vide est refusé en 400 sur toutes les créations")
    void emptyBodyReturns400(String url) throws Exception {
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"));
    }
}

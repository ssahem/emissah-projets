package com.gestionlocative.gestion_locative_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestionlocative.gestion_locative_api.dto.request.CreateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleAppartementsLibresResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PeriodeOccupationResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ProprietaireResponse;
import com.gestionlocative.gestion_locative_api.dto.response.RevenuImmeubleResponse;
import com.gestionlocative.gestion_locative_api.exception.BailStatutInvalideException;
import com.gestionlocative.gestion_locative_api.exception.GlobalExceptionHandler;
import com.gestionlocative.gestion_locative_api.exception.ImmeubleNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.MoisInvalideException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireEmailDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.RenouvellementDateInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerDejaExistanteException;
import com.gestionlocative.gestion_locative_api.service.AppartementService;
import com.gestionlocative.gestion_locative_api.service.BailService;
import com.gestionlocative.gestion_locative_api.service.ImmeubleService;
import com.gestionlocative.gestion_locative_api.service.LocataireService;
import com.gestionlocative.gestion_locative_api.service.ProprietaireService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests web (tranche contrôleur) des codes de réponse documentés.
 *
 * COUCHE       : Service
 * RÔLE         : Tests web (tranche contrôleur) des codes de réponse documentés.
 * TYPE         : Web (slice @WebMvcTest)
 * STRATÉGIE    : services simulés, GlobalExceptionHandler importé, sans base.
 * CAS COUVERTS : 201 + Location, 204, 400 (format et path), 404 et 409 (erreurs métier).
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ControllerWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @MockitoBean private ProprietaireService proprietaireService;
    @MockitoBean private ImmeubleService immeubleService;
    @MockitoBean private AppartementService appartementService;
    @MockitoBean private LocataireService locataireService;

    // ── create_returns201WithLocation() ──────────────────────────────────────

    @MockitoBean private BailService bailService;

    @Test
    @DisplayName("La création renvoie 201 avec un en-tête Location")
    void create_returns201WithLocation() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        ProprietaireResponse response = new ProprietaireResponse(
                1, "Diallo", "Awa", "awa@example.test", "0102030405");
        when(proprietaireService.create(any(CreateProprietaireRequest.class))).thenReturn(response);

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/proprietaire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Diallo", "prenom", "Awa",
                                "email", "awa@example.test", "telephone", "0102030405"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/gestion-locative/proprietaire/get/1"))
                .andExpect(jsonPath("$.idProprietaire").value(1));
    }

    // ── create_returns400WhenCorpsInvalide() ─────────────────────────────────

    @Test
    @DisplayName("Un corps invalide renvoie 400 avec la liste des champs")
    void create_returns400WhenCorpsInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/proprietaire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"prenom\":\"Awa\","
                                + "\"email\":\"pas-un-email\",\"telephone\":\"0102\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"))
                .andExpect(jsonPath("$.champs").isArray());
    }

    // ── getById_returns400WhenIdInvalide() ───────────────────────────────────

    @Test
    @DisplayName("Un identifiant nul ou négatif renvoie 400")
    void getById_returns400WhenIdInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/proprietaire/get/0"))
                .andExpect(status().isBadRequest());
    }

    // ── getById_returns404WhenIntrouvable() ──────────────────────────────────

    @Test
    @DisplayName("Une erreur métier 404 est traduite en Problem Detail")
    void getById_returns404WhenIntrouvable() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(proprietaireService.findById(99)).thenThrow(new ProprietaireNotFoundException(99));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/proprietaire/get/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Propriétaire introuvable"))
                .andExpect(jsonPath("$.idProprietaire").value(99));
    }

    // ── create_returns409WhenEmailDejaUtilise() ──────────────────────────────

    @Test
    @DisplayName("Un conflit métier est traduit en Problem Detail 409")
    void create_returns409WhenEmailDejaUtilise() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(proprietaireService.create(any(CreateProprietaireRequest.class)))
                .thenThrow(new ProprietaireEmailDejaUtiliseException("awa@example.test"));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/proprietaire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Diallo", "prenom", "Awa",
                                "email", "awa@example.test", "telephone", "0102030405"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email déjà utilisé"))
                .andExpect(jsonPath("$.email").value("awa@example.test"));
    }

    // ── delete_returns204() ──────────────────────────────────────────────────

    @Test
    @DisplayName("La suppression renvoie 204")
    void delete_returns204() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(delete("/gestion-locative/proprietaire/delete/1"))
                .andExpect(status().isNoContent());
    }

    // ── resilier_returns409WhenStatutInvalide() ──────────────────────────────

    @Test
    @DisplayName("F13 — une résiliation impossible renvoie 409")
    void resilier_returns409WhenStatutInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailService.resilier(any(), any()))
                .thenThrow(new BailStatutInvalideException(5, "le bail est résilié"));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/resiliation/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateEffet\":\"2026-09-30\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Action impossible sur ce bail"))
                .andExpect(jsonPath("$.idBail").value(5));
    }

    // ── renouveler_returns400WhenDateInvalide() ──────────────────────────────

    @Test
    @DisplayName("F14 — une échéance de renouvellement invalide renvoie 400")
    void renouveler_returns400WhenDateInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailService.renouveler(any(), any()))
                .thenThrow(new RenouvellementDateInvalideException(
                        5, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 12, 1)));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/renouvellement/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateFin\":\"2026-12-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Date de renouvellement invalide"));
    }

    // ── reviser_returns409WhenRevisionDejaExistante() ────────────────────────

    @Test
    @DisplayName("F15 — une révision déjà présente renvoie 409")
    void reviser_returns409WhenRevisionDejaExistante() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailService.reviserLoyer(any(), any()))
                .thenThrow(new RevisionLoyerDejaExistanteException(
                        5, LocalDate.of(2027, 1, 1)));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/revision/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montant\":920.00,\"dateEffet\":\"2027-01-01\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Révision déjà existante"));
    }

    // ── reviser_returns400WhenMontantInvalide() ──────────────────────────────

    @Test
    @DisplayName("F15 — un montant non positif renvoie 400")
    void reviser_returns400WhenMontantInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/gestion-locative/bail/revision/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montant\":0,\"dateEffet\":\"2027-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"));
    }

    // ── libres_sansDate_appelleServiceAvecNull() ─────────────────────────────

    @Test
    @DisplayName("F17 — la date du jour est utilisée quand le paramètre est absent")
    void libres_sansDate_appelleServiceAvecNull() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/libres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        org.mockito.Mockito.verify(appartementService).findLibres(null);
    }

    // ── libres_returns400WhenDateInvalide() ──────────────────────────────────

    @Test
    @DisplayName("F17 — un format de date invalide renvoie 400")
    void libres_returns400WhenDateInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/libres").param("date", "01/12/2026"))
                .andExpect(status().isBadRequest());
    }

    // ── libres_returnsGroupement() ───────────────────────────────────────────

    @Test
    @DisplayName("F17 — les appartements libres sont exposés groupés par immeuble")
    void libres_returnsGroupement() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(appartementService.findLibres(LocalDate.of(2026, 12, 1))).thenReturn(List.of(
                new ImmeubleAppartementsLibresResponse(1, "12 rue des Acacias", "Paris", List.of())));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/libres").param("date", "2026-12-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idImmeuble").value(1))
                .andExpect(jsonPath("$[0].ville").value("Paris"));
    }

    // ── occupation_returnsPeriodes() ─────────────────────────────────────────

    @Test
    @DisplayName("F18 — l'occupation est exposée avec ses périodes")
    void occupation_returnsPeriodes() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(appartementService.findOccupation(2)).thenReturn(List.of(
                new PeriodeOccupationResponse(LocalDate.of(2026, 6, 15), null,
                        "VACANT", null, null, null, null)));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/appartement/occupation/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("VACANT"))
                .andExpect(jsonPath("$[0].debut").value("2026-06-15"));
    }

    // ── revenu_returnsDetail() ───────────────────────────────────────────────

    @Test
    @DisplayName("F19 — le revenu est exposé avec son détail")
    void revenu_returnsDetail() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleService.calculerRevenu(1, "2026-11")).thenReturn(
                new RevenuImmeubleResponse(1, "2026-11", new BigDecimal("440.00"),
                        new BigDecimal("50.00"), List.of()));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/immeuble/revenu/1").param("mois", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mois").value("2026-11"))
                .andExpect(jsonPath("$.revenuTotal").value(440.00));
    }

    // ── revenu_returns400WhenMoisInvalide() ──────────────────────────────────

    @Test
    @DisplayName("F19 — un mois mal formé renvoie 400")
    void revenu_returns400WhenMoisInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleService.calculerRevenu(1, "2026-13"))
                .thenThrow(new MoisInvalideException("2026-13"));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/immeuble/revenu/1").param("mois", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Mois invalide"))
                .andExpect(jsonPath("$.mois").value("2026-13"));
    }

    // ── revenu_returns404WhenImmeubleInconnu() ───────────────────────────────

    @Test
    @DisplayName("F19 — un immeuble inconnu renvoie 404")
    void revenu_returns404WhenImmeubleInconnu() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleService.calculerRevenu(99, "2026-11"))
                .thenThrow(new ImmeubleNotFoundException(99));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/gestion-locative/immeuble/revenu/99").param("mois", "2026-11"))
                .andExpect(status().isNotFound());
    }
}

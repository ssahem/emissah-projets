package com.interventionsterrain.interventions_terrain_api.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.HistoriqueClientResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.PlanningInterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienEffectifResponse;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.GlobalExceptionHandler;
import com.interventionsterrain.interventions_terrain_api.service.AgenceService;
import com.interventionsterrain.interventions_terrain_api.service.ClientService;
import com.interventionsterrain.interventions_terrain_api.service.CompetenceService;
import com.interventionsterrain.interventions_terrain_api.service.InterventionService;
import com.interventionsterrain.interventions_terrain_api.service.RestitutionService;
import com.interventionsterrain.interventions_terrain_api.service.TechnicienService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Codes de réponse HTTP et paramètres des consultations F6 à F10.
 *
 * COUCHE       : Service
 * RÔLE         : Codes de réponse HTTP et paramètres des consultations F6 à F10.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ConsultationWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AgenceService agenceService;
    @MockitoBean private TechnicienService technicienService;
    @MockitoBean private CompetenceService competenceService;
    @MockitoBean private ClientService clientService;
    @MockitoBean private InterventionService interventionService;

    // ── f6Effectif() ─────────────────────────────────────────────────────────

    @MockitoBean private RestitutionService restitutionService;

    @Test
    @DisplayName("F6 : GET agence/techniciens/{id} renvoie l'effectif")
    void f6Effectif() throws Exception {
        when(agenceService.findTechniciens(1)).thenReturn(List.of(
                new TechnicienEffectifResponse(3, "Alice", "Prenom", "MAT003", 2L)));

        mockMvc.perform(get("/interventions-terrain/agence/techniciens/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Alice"))
                .andExpect(jsonPath("$[0].nbCompetencesValides").value(2));
    }

    // ── f7Competences() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F7 : GET technicien/competences/{id} signale les compétences expirées")
    void f7Competences() throws Exception {
        when(technicienService.findCompetences(3)).thenReturn(List.of(
                new CompetenceTechnicienResponse(5, "Fibre", LocalDate.of(2024, 1, 10),
                        LocalDate.of(2025, 1, 10), true)));

        mockMvc.perform(get("/interventions-terrain/technicien/competences/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expiree").value(true));
    }

    // ── f8Historique() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F8 : GET client/interventions/{id} renvoie l'historique")
    void f8Historique() throws Exception {
        when(clientService.findHistorique(7)).thenReturn(List.of(new HistoriqueClientResponse(
                20, LocalDateTime.of(2026, 11, 20, 9, 0), LocalDateTime.of(2026, 11, 25, 9, 0),
                LocalDateTime.of(2026, 11, 25, 12, 0), "RACCORDEMENT_FIBRE", 3, "Alice Prenom",
                "TERMINEE")));

        mockMvc.perform(get("/interventions-terrain/client/interventions/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("TERMINEE"));
    }

    // ── f9Filtres() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : GET intervention/list transmet les filtres au service")
    void f9Filtres() throws Exception {
        when(interventionService.findAll("PLANIFIEE", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7)))
                .thenReturn(List.of(new InterventionResponse(20, 7, "RACCORDEMENT_FIBRE",
                        LocalDateTime.of(2026, 11, 20, 9, 0), LocalDateTime.of(2026, 12, 2, 9, 0),
                        LocalDateTime.of(2026, 12, 2, 12, 0), "12 rue de la Paix", "Paris", 3)));

        mockMvc.perform(get("/interventions-terrain/intervention/list")
                        .param("statut", "PLANIFIEE")
                        .param("du", "2026-12-01")
                        .param("au", "2026-12-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeType").value("RACCORDEMENT_FIBRE"));

        verify(interventionService).findAll("PLANIFIEE",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7));
    }

    // ── f9StatutInconnu() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : un statut inconnu renvoie 400 en Problem Details")
    void f9StatutInconnu() throws Exception {
        when(interventionService.findAll("INCONNU", null, null))
                .thenThrow(new DonneeInvalideException("statut-inconnu", "Le statut « INCONNU » est inconnu"));

        mockMvc.perform(get("/interventions-terrain/intervention/list").param("statut", "INCONNU"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/statut-inconnu"));
    }

    // ── f10Planning() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("F10 : GET technicien/planning/{id} accepte une période explicite")
    void f10Planning() throws Exception {
        when(technicienService.findPlanning(3, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(new PlanningInterventionResponse(20,
                        LocalDateTime.of(2026, 12, 2, 9, 0), LocalDateTime.of(2026, 12, 2, 12, 0),
                        "Dupont", "Marie", "12 rue de la Paix", "Paris", false)));

        mockMvc.perform(get("/interventions-terrain/technicien/planning/3")
                        .param("du", "2026-12-01")
                        .param("au", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomClient").value("Dupont"))
                .andExpect(jsonPath("$[0].annulee").value(false));
    }
}

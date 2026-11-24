package com.interventionsterrain.interventions_terrain_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateObtentionCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateRepriseRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateStatutRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
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

/** Codes de réponse HTTP et corps des règles métier F11, F14 et F15. */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ReglesMetierWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @MockitoBean private AgenceService agenceService;
    @MockitoBean private TechnicienService technicienService;
    @MockitoBean private CompetenceService competenceService;
    @MockitoBean private ClientService clientService;
    @MockitoBean private InterventionService interventionService;
    @MockitoBean private RestitutionService restitutionService;

    @Test
    @DisplayName("F11 : POST technicien/competence/{id} renvoie 201")
    void f11AssociationRenvoie201() throws Exception {
        when(technicienService.associerCompetence(eq(3), any(CreateObtentionCompetenceRequest.class)))
                .thenReturn(new CompetenceTechnicienResponse(5, "Fibre",
                        LocalDate.of(2026, 1, 10), LocalDate.of(2027, 1, 10), false));

        mockMvc.perform(post("/interventions-terrain/technicien/competence/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateObtentionCompetenceRequest(
                                5, LocalDate.of(2026, 1, 10), LocalDate.of(2027, 1, 10)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.libelle").value("Fibre"));
    }

    @Test
    @DisplayName("F11 : des dates incohérentes renvoient 400 en Problem Details")
    void f11DatesIncoherentesRenvoient400() throws Exception {
        when(technicienService.associerCompetence(eq(3), any(CreateObtentionCompetenceRequest.class)))
                .thenThrow(new DonneeInvalideException("dates-competence-incoherentes",
                        "La date d'expiration doit être postérieure à la date d'obtention"));

        mockMvc.perform(post("/interventions-terrain/technicien/competence/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateObtentionCompetenceRequest(
                                5, LocalDate.of(2026, 1, 10), LocalDate.of(2025, 1, 10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/dates-competence-incoherentes"));
    }

    @Test
    @DisplayName("F14 : POST intervention/statut/{id} accepte une transition autorisée")
    void f14ChangementStatutRenvoie200() throws Exception {
        when(interventionService.changerStatut(eq(20), any(UpdateStatutRequest.class)))
                .thenReturn(intervention(20));

        mockMvc.perform(post("/interventions-terrain/intervention/statut/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"EN_COURS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIntervention").value(20));
        verify(interventionService).changerStatut(20, new UpdateStatutRequest("EN_COURS"));
    }

    @Test
    @DisplayName("F14 : une transition interdite renvoie 409 avec les statuts atteignables")
    void f14TransitionInterditeRenvoie409() throws Exception {
        when(interventionService.changerStatut(eq(20), any(UpdateStatutRequest.class)))
                .thenThrow(new ConflitMetierException("transition-interdite",
                        "Transition refusée", Map.of("statutCourant", "EN_COURS",
                        "atteignables", List.of("ECHOUEE", "TERMINEE"))));

        mockMvc.perform(post("/interventions-terrain/intervention/statut/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"ANNULEE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statutCourant").value("EN_COURS"))
                .andExpect(jsonPath("$.atteignables[0]").value("ECHOUEE"));
    }

    @Test
    @DisplayName("F15 : POST intervention/reprise/{id} renvoie 201 et un en-tête Location")
    void f15RepriseRenvoie201() throws Exception {
        when(interventionService.creerReprise(eq(20), any(CreateRepriseRequest.class)))
                .thenReturn(intervention(21));

        mockMvc.perform(post("/interventions-terrain/intervention/reprise/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRepriseRequest(
                                LocalDateTime.of(2026, 12, 17, 9, 0),
                                LocalDateTime.of(2026, 12, 17, 12, 0)))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/interventions-terrain/intervention/get/21"));
    }

    private InterventionResponse intervention(Integer id) {
        return new InterventionResponse(id, 7, "RACCORDEMENT_FIBRE",
                LocalDateTime.of(2026, 11, 24, 9, 0),
                LocalDateTime.of(2026, 12, 17, 9, 0), LocalDateTime.of(2026, 12, 17, 12, 0),
                "12 rue de la Paix", "Paris", 3);
    }
}

package com.interventionsterrain.interventions_terrain_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interventionsterrain.interventions_terrain_api.dto.request.AddAffectationRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AffectationResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.GlobalExceptionHandler;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.service.AgenceService;
import com.interventionsterrain.interventions_terrain_api.service.ClientService;
import com.interventionsterrain.interventions_terrain_api.service.CompetenceService;
import com.interventionsterrain.interventions_terrain_api.service.InterventionService;
import com.interventionsterrain.interventions_terrain_api.service.RestitutionService;
import com.interventionsterrain.interventions_terrain_api.service.TechnicienService;
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

/** Codes de réponse HTTP des cinq CRUD (F1 à F5). */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class ControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @MockitoBean private AgenceService agenceService;
    @MockitoBean private TechnicienService technicienService;
    @MockitoBean private CompetenceService competenceService;
    @MockitoBean private ClientService clientService;
    @MockitoBean private InterventionService interventionService;
    @MockitoBean private RestitutionService restitutionService;

    @Test
    @DisplayName("POST agence/create renvoie 201 et un en-tête Location")
    void createAgenceReturns201() throws Exception {
        when(agenceService.create(any(CreateAgenceRequest.class)))
                .thenReturn(new AgenceResponse(1, "Paris Centre", "Paris", "Paris", "Mme Durand"));

        mockMvc.perform(post("/interventions-terrain/agence/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Paris Centre", "ville", "Paris"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/interventions-terrain/agence/get/1"))
                .andExpect(jsonPath("$.ville").value("Paris"));
    }

    @Test
    @DisplayName("GET agence/list renvoie la liste triée")
    void listAgencesReturns200() throws Exception {
        when(agenceService.findAll()).thenReturn(List.of(
                new AgenceResponse(1, "Agence A", "Lyon", "Lyon", null),
                new AgenceResponse(2, "Agence B", "Paris", "Paris", null)));

        mockMvc.perform(get("/interventions-terrain/agence/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ville").value("Lyon"))
                .andExpect(jsonPath("$[1].ville").value("Paris"));
    }

    @Test
    @DisplayName("Un corps invalide renvoie 400 avec les champs en erreur")
    void createTechnicienInvalidReturns400() throws Exception {
        mockMvc.perform(post("/interventions-terrain/technicien/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"idAgence\":0,\"matricule\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corps invalide"))
                .andExpect(jsonPath("$.champs").isArray());
    }

    @Test
    @DisplayName("Une ressource absente renvoie 404 en Problem Details")
    void getClientIntrouvableReturns404() throws Exception {
        when(clientService.findById(99)).thenThrow(new RessourceIntrouvableException("Client", 99));

        mockMvc.perform(get("/interventions-terrain/client/get/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/ressource-introuvable"));
    }

    @Test
    @DisplayName("Un conflit métier renvoie 409 avec le type de problème")
    void createCompetenceConflitReturns409() throws Exception {
        when(competenceService.create(any()))
                .thenThrow(new ConflitMetierException("competence-libelle-deja-utilise", "Libellé pris"));

        mockMvc.perform(post("/interventions-terrain/competence/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libelle\":\"Fibre\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/competence-libelle-deja-utilise"));
    }

    @Test
    @DisplayName("DELETE intervention/delete/{id} renvoie 204")
    void deleteInterventionReturns204() throws Exception {
        doNothing().when(interventionService).delete(4);

        mockMvc.perform(delete("/interventions-terrain/intervention/delete/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT intervention/update/{id} renvoie le créneau modifié")
    void updateInterventionReturns200() throws Exception {
        when(interventionService.update(eq(4), any(UpdateInterventionRequest.class)))
                .thenReturn(interventionResponse());

        mockMvc.perform(put("/interventions-terrain/intervention/update/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "debutPrevu", "2026-12-02T09:00:00",
                                "finPrevue", "2026-12-02T12:00:00",
                                "idTechnicienResponsable", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTechnicienResponsable").value(2))
                .andExpect(jsonPath("$.voie").value("12 rue de la Paix"));
    }

    @Test
    @DisplayName("POST intervention/{id}/affecter renvoie 201")
    void addAffectationReturns201() throws Exception {
        when(interventionService.addAffectation(eq(4), any(AddAffectationRequest.class)))
                .thenReturn(new AffectationResponse(11, 4, 2, false));

        mockMvc.perform(post("/interventions-terrain/intervention/affectation/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTechnicien\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTechnicien").value(2))
                .andExpect(jsonPath("$.estResponsable").value(false));
    }

    /** Réponse d'intervention nominative. */
    private InterventionResponse interventionResponse() {
        return new InterventionResponse(4, 1, "RACCORDEMENT_FIBRE",
                LocalDateTime.of(2026, 11, 24, 9, 0),
                LocalDateTime.of(2026, 12, 2, 9, 0),
                LocalDateTime.of(2026, 12, 2, 12, 0),
                "12 rue de la Paix", "Paris", 2);
    }
}

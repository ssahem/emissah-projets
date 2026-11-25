package com.interventionsterrain.interventions_terrain_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceDisponibleResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.DisponiblesResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxReussiteResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxTypeResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienDisponibleResponse;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.GlobalExceptionHandler;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.service.AgenceService;
import com.interventionsterrain.interventions_terrain_api.service.ClientService;
import com.interventionsterrain.interventions_terrain_api.service.CompetenceService;
import com.interventionsterrain.interventions_terrain_api.service.InterventionService;
import com.interventionsterrain.interventions_terrain_api.service.RestitutionService;
import com.interventionsterrain.interventions_terrain_api.service.TechnicienService;
import java.math.BigDecimal;
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
 * Codes de réponse et sérialisation des routes de restitution F17 à F19.
 *
 * COUCHE       : Service
 * RÔLE         : Codes de réponse et sérialisation des routes de restitution F17 à F19.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class RestitutionWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 10, 9, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 10, 12, 0);

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AgenceService agenceService;
    @MockitoBean private TechnicienService technicienService;
    @MockitoBean private CompetenceService competenceService;
    @MockitoBean private ClientService clientService;
    @MockitoBean private InterventionService interventionService;

    // ── f17DisponiblesRenvoie200() ───────────────────────────────────────────

    @MockitoBean private RestitutionService restitutionService;

    @Test
    @DisplayName("F17 : GET technicien/disponibles renvoie les agences et la charge du jour")
    void f17DisponiblesRenvoie200() throws Exception {
        when(restitutionService.findDisponibles(DEBUT, FIN, "RACCORDEMENT_FIBRE"))
                .thenReturn(new DisponiblesResponse(List.of(new AgenceDisponibleResponse(1,
                        "Agence Nord", List.of(new TechnicienDisponibleResponse(
                                3, "Alice", "Martin", "MAT003", new BigDecimal("2.00"))))), null));

        mockMvc.perform(get("/interventions-terrain/technicien/disponibles")
                        .param("debut", "2026-12-10T09:00:00")
                        .param("fin", "2026-12-10T12:00:00")
                        .param("type", "RACCORDEMENT_FIBRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences[0].idAgence").value(1))
                .andExpect(jsonPath("$.agences[0].techniciens[0].chargeHeuresJour").value(2.00))
                .andExpect(jsonPath("$.raisonFiltrage").doesNotExist());
    }

    // ── f17AucunResultatRenvoieRaison() ──────────────────────────────────────

    @Test
    @DisplayName("F17 : une liste vide porte la raison du filtrage")
    void f17AucunResultatRenvoieRaison() throws Exception {
        when(restitutionService.findDisponibles(any(), any(), any()))
                .thenReturn(new DisponiblesResponse(List.of(), "aucun technicien compétent"));

        mockMvc.perform(get("/interventions-terrain/technicien/disponibles")
                        .param("debut", "2026-12-10T09:00:00")
                        .param("fin", "2026-12-10T12:00:00")
                        .param("type", "RACCORDEMENT_FIBRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences").isEmpty())
                .andExpect(jsonPath("$.raisonFiltrage").value("aucun technicien compétent"));
    }

    // ── f18ChargeRenvoie200() ────────────────────────────────────────────────

    @Test
    @DisplayName("F18 : GET agence/charge/{id} renvoie le détail et les totaux")
    void f18ChargeRenvoie200() throws Exception {
        LocalDateTime du = LocalDateTime.of(2026, 12, 1, 0, 0);
        LocalDateTime au = LocalDateTime.of(2026, 12, 2, 0, 0);
        when(restitutionService.findCharge(1, du, au))
                .thenReturn(new ChargeAgenceResponse(1, "Agence Nord",
                        List.of(new ChargeTechnicienResponse(3, "Alice", "Martin", 1,
                                new BigDecimal("3.00"))), 1, new BigDecimal("3.00")));

        mockMvc.perform(get("/interventions-terrain/agence/charge/1")
                        .param("du", "2026-12-01T00:00:00")
                        .param("au", "2026-12-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomAgence").value("Agence Nord"))
                .andExpect(jsonPath("$.techniciens[0].nombreInterventions").value(1))
                .andExpect(jsonPath("$.heuresOccupeesTotales").value(3.00));
    }

    // ── f18AgenceInconnueRenvoie404() ────────────────────────────────────────

    @Test
    @DisplayName("F18 : une agence inconnue renvoie 404")
    void f18AgenceInconnueRenvoie404() throws Exception {
        when(restitutionService.findCharge(eq(99), any(), any()))
                .thenThrow(new RessourceIntrouvableException("Agence", 99));

        mockMvc.perform(get("/interventions-terrain/agence/charge/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/ressource-introuvable"));
    }

    // ── f19TauxRenvoie200() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : GET agence/taux-reussite/{id} renvoie le taux global et par type")
    void f19TauxRenvoie200() throws Exception {
        when(restitutionService.findTauxReussite(1, "2026-11"))
                .thenReturn(new TauxReussiteResponse(1, "2026-11", 1, 1,
                        new BigDecimal("50.00"), List.of(new TauxTypeResponse(
                                "RACCORDEMENT_FIBRE", 1, 1, new BigDecimal("50.00")))));

        mockMvc.perform(get("/interventions-terrain/agence/taux-reussite/1")
                        .param("mois", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tauxGlobal").value(50.00))
                .andExpect(jsonPath("$.parType[0].codeType").value("RACCORDEMENT_FIBRE"));
    }

    // ── f19MoisInvalideRenvoie400() ──────────────────────────────────────────

    @Test
    @DisplayName("F19 : un mois invalide renvoie 400 en Problem Details")
    void f19MoisInvalideRenvoie400() throws Exception {
        when(restitutionService.findTauxReussite(1, "11/2026")).thenThrow(
                new DonneeInvalideException("mois-invalide",
                        "Le mois doit être au format yyyy-MM, par exemple 2026-11"));

        mockMvc.perform(get("/interventions-terrain/agence/taux-reussite/1")
                        .param("mois", "11/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://api.interventions-terrain.local/problems/mois-invalide"));
    }
}

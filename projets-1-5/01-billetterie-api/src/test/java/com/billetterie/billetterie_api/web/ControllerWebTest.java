package com.billetterie.billetterie_api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.billetterie.billetterie_api.dto.request.CreateSalleRequest;
import com.billetterie.billetterie_api.dto.request.UpdatePlaceRequest;
import com.billetterie.billetterie_api.dto.response.AnnulationResponse;
import com.billetterie.billetterie_api.exception.PeriodeInvalideException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.service.EvenementService;
import com.billetterie.billetterie_api.service.PlaceService;
import com.billetterie.billetterie_api.service.ReservationService;
import com.billetterie.billetterie_api.service.SalleService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de la couche web : validation JSON, formats de paramètres et traduction des erreurs.
 *
 * COUCHE       : Service
 * RÔLE         : Tests de la couche web : validation JSON, formats de paramètres et traduction des erreurs.
 * TYPE         : Web (@WebMvcTest, sans contexte JPA ni base de données)
 * STRATÉGIE    : les services sont simulés ; le GlobalExceptionHandler réel est chargé.
 * CAS COUVERTS : 400 de validation, 400 d'id invalide, 404 métier, 204, 400 de date, 400 métier, corps d'annulation.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@WebMvcTest
class ControllerWebTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SalleService salleService;
    @MockitoBean private PlaceService placeService;
    @MockitoBean private EvenementService evenementService;

    // ── createSalle_capaciteInvalide() ───────────────────────────────────────

    @MockitoBean private ReservationService reservationService;

    @Test
    @DisplayName("Une capacité nulle est refusée avec un 400 de validation")
    void createSalle_capaciteInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        String corps = """
                {"nom":"Grand Hall","adresse":"12 rue des Arts","ville":"Abidjan","capacite":0}
                """;

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/billetterie/salle/create")
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isBadRequest());
    }

    // ── getSalle_introuvable() ───────────────────────────────────────────────

    @Test
    @DisplayName("Une salle introuvable produit un Problem Detail 404")
    void getSalle_introuvable() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleService.findById(999)).thenThrow(new SalleNotFoundException(999));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/billetterie/salle/get/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Salle introuvable"))
                .andExpect(jsonPath("$.idSalle").value(999));
    }

    // ── getSalle_idInvalide() ────────────────────────────────────────────────

    @Test
    @DisplayName("Un identifiant non positif est refusé avant d'atteindre le service")
    void getSalle_idInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/billetterie/salle/get/{id}", 0))
                .andExpect(status().isBadRequest());
    }

    // ── deletePlace_noContent() ──────────────────────────────────────────────

    @Test
    @DisplayName("La suppression d'une place vide renvoie 204")
    void deletePlace_noContent() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(delete("/billetterie/place/delete/{id}", 5))
                .andExpect(status().isNoContent());
        verify(placeService).delete(5);
    }

    // ── listEvenements_dateInvalide() ────────────────────────────────────────

    @Test
    @DisplayName("Une borne de date au format invalide est refusée avec 400")
    void listEvenements_dateInvalide() throws Exception {
        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/billetterie/evenement/list").param("du", "2026-12-01"))
                .andExpect(status().isBadRequest());
    }

    // ── listEvenements_periodeInversee() ─────────────────────────────────────

    @Test
    @DisplayName("Une période inversée remonte le titre métier")
    void listEvenements_periodeInversee() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(evenementService.findBetween(any(), any()))
                .thenThrow(new PeriodeInvalideException("2026-12-31T00:00:00"));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(get("/billetterie/evenement/list")
                        .param("du", "2026-12-31T00:00:00")
                        .param("au", "2026-12-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Période invalide"));
    }

    // ── annulerReservation_ok() ──────────────────────────────────────────────

    @Test
    @DisplayName("L'annulation renvoie le montant remboursé")
    void annulerReservation_ok() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        when(reservationService.annuler(12)).thenReturn(new AnnulationResponse(
                12, "ANNULEE", new BigDecimal("12.50"), LocalDateTime.of(2026, 12, 1, 20, 0)));

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(post("/billetterie/reservation/annulation/{id}", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"))
                .andExpect(jsonPath("$.montantRembourse").value(12.50));
    }

    // ── updatePlace_corpsInvalide() ──────────────────────────────────────────

    @Test
    @DisplayName("La modification d'une place valide le corps JSON")
    void updatePlace_corpsInvalide() throws Exception {
        // ── Préparation ──────────────────────────────────────────────────────
        String corps = """
                {"idSalle":1,"rang":"","numero":13,"categorie":"ORCHESTRE"}
                """;

        // ── Action + Vérification ────────────────────────────────────────────
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/billetterie/place/update/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verify(placeService, org.mockito.Mockito.never())
                .update(eq(5), any(UpdatePlaceRequest.class));
    }
}

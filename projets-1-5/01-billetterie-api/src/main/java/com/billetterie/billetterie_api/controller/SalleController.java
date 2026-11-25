package com.billetterie.billetterie_api.controller;

import com.billetterie.billetterie_api.dto.request.CreateSalleRequest;
import com.billetterie.billetterie_api.dto.response.PlaceSalleResponse;
import com.billetterie.billetterie_api.dto.response.ProgrammationResponse;
import com.billetterie.billetterie_api.dto.response.SalleResponse;
import com.billetterie.billetterie_api.dto.request.UpdateSalleRequest;
import com.billetterie.billetterie_api.service.SalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST des salles et de leurs consultations.
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /billetterie/salle
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id} · places/{id} · evenements/{id}
 * NE FAIT PAS  : de logique métier ni de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/billetterie/salle", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Salles", description = "Gestion des salles, de leurs places et de leur programmation")
public class SalleController {

    private final SalleService salleService;

    public SalleController(SalleService salleService) {
        this.salleService = salleService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /billetterie/salle/list ─────────────────────────────────

    /**
     * Liste les salles. Réponses : 200 | 500.
     */

    @Operation(summary = "Lister les salles")
    @ApiResponse(responseCode = "200", description = "Salles renvoyées")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<SalleResponse> list() {
        return salleService.findAll();
    }

    // ── getById() — GET /billetterie/salle/get/{id} ──────────────────────────

    /**
     * Consulte une salle. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter une salle")
    @ApiResponse(responseCode = "200", description = "Salle renvoyée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public SalleResponse getById(@PathVariable @Positive Integer id) {
        return salleService.findById(id);
    }

    // ── create() — POST /billetterie/salle/create ────────────────────────────

    /**
     * Crée une salle. Réponses : 201 | 400 | 409 | 500.
     */

    @Operation(summary = "Créer une salle")
    @ApiResponse(responseCode = "201", description = "Salle créée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "409", description = "Salle déjà existante")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SalleResponse> create(@Valid @RequestBody CreateSalleRequest request) {
        SalleResponse created = salleService.create(request);
        return ResponseEntity.created(URI.create("/billetterie/salle/get/" + created.idSalle())).body(created);
    }

    // ── update() — PUT /billetterie/salle/update/{id} ────────────────────────

    /**
     * Modifie une salle. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Modifier une salle")
    @ApiResponse(responseCode = "200", description = "Salle modifiée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "409", description = "Capacité ou nom impossible")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalleResponse update(@PathVariable @Positive Integer id, @Valid @RequestBody UpdateSalleRequest request) {
        return salleService.update(id, request);
    }

    // ── delete() — DELETE /billetterie/salle/delete/{id} ─────────────────────

    /**
     * Supprime une salle vide. Réponses : 204 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Supprimer une salle")
    @ApiResponse(responseCode = "204", description = "Salle supprimée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "409", description = "Salle encore utilisée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        salleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── places() — GET /billetterie/salle/places/{id} ────────────────────────

    /**
     * Liste les places d'une salle, avec filtre catégorie optionnel (F5, F6).
     * Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Lister les places d'une salle")
    @ApiResponse(responseCode = "200", description = "Places renvoyées")
    @ApiResponse(responseCode = "400", description = "Catégorie inconnue")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/places/{id}")
    public List<PlaceSalleResponse> places(@PathVariable @Positive Integer id,
                                           @RequestParam(required = false) String categorie) {
        return salleService.findPlaces(id, categorie);
    }

    // ── programmation() — GET /billetterie/salle/evenements/{id} ─────────────

    /**
     * Retourne la programmation d'une salle (F7). Réponses : 200 | 400 | 404 |
     * 500.
     */

    @Operation(summary = "Lister les événements d'une salle")
    @ApiResponse(responseCode = "200", description = "Programmation renvoyée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/evenements/{id}")
    public List<ProgrammationResponse> programmation(@PathVariable @Positive Integer id) {
        return salleService.findProgrammation(id);
    }
}

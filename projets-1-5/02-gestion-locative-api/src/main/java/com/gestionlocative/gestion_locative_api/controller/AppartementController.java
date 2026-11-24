package com.gestionlocative.gestion_locative_api.controller;

import com.gestionlocative.gestion_locative_api.dto.request.CreateAppartementRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateAppartementRequest;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementResponse;
import com.gestionlocative.gestion_locative_api.dto.response.HistoriqueBailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleAppartementsLibresResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PeriodeOccupationResponse;
import com.gestionlocative.gestion_locative_api.service.AppartementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
 * Contrôleur REST des appartements (F3).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /gestion-locative/appartement
 * ENDPOINTS    : list (recherche F9) · get/{id} · baux/{id} · libres (F17)
 *                · occupation/{id} (F18) · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique documentée dans le README.
 * DÉPEND DE    : AppartementService uniquement.
 * NE FAIT PAS  : de logique métier, d'accès base, de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/gestion-locative/appartement", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Appartements", description = "Gestion des appartements (F3)")
public class AppartementController {

    private final AppartementService appartementService;

    /** Injection du service par Spring. */
    public AppartementController(AppartementService appartementService) {
        this.appartementService = appartementService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /gestion-locative/appartement/list ─────────────────────

    /**
     * Liste les appartements, avec recherche optionnelle par critères (F3, F9).
     * Réponses : 200 | 400 | 500.
     */
    @Operation(summary = "Lister ou rechercher les appartements")
    @ApiResponse(responseCode = "200", description = "Appartements renvoyés")
    @ApiResponse(responseCode = "400", description = "Bornes de surface invalides")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<AppartementResponse> list(
            @RequestParam(required = false) BigDecimal surfaceMin,
            @RequestParam(required = false) BigDecimal surfaceMax,
            @RequestParam(name = "pieces", required = false) Integer nbPieces) {
        if (surfaceMin == null && surfaceMax == null && nbPieces == null) {
            return appartementService.findAll();
        }
        return appartementService.search(surfaceMin, surfaceMax, nbPieces);
    }

    // ── getById() — GET /gestion-locative/appartement/get/{id} ──────────────

    /** Consulte un appartement. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Consulter un appartement")
    @ApiResponse(responseCode = "200", description = "Appartement renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Appartement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public AppartementResponse getById(@PathVariable @Positive Integer id) {
        return appartementService.findById(id);
    }

    // ── baux() — GET /gestion-locative/appartement/baux/{id} ────────────────

    /** Consulte l'historique des baux. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Consulter l'historique des baux d'un appartement")
    @ApiResponse(responseCode = "200", description = "Historique renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Appartement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/baux/{id}")
    public List<HistoriqueBailResponse> baux(@PathVariable @Positive Integer id) {
        return appartementService.findBaux(id);
    }

    // ── libres() — GET /gestion-locative/appartement/libres ─────────────────

    /** Liste les appartements disponibles. Réponses : 200 | 400 | 500. */
    @Operation(summary = "Lister les appartements libres à une date")
    @ApiResponse(responseCode = "200", description = "Appartements groupés par immeuble")
    @ApiResponse(responseCode = "400", description = "Format de date invalide")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/libres")
    public List<ImmeubleAppartementsLibresResponse> libres(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appartementService.findLibres(date);
    }

    // ── occupation() — GET /gestion-locative/appartement/occupation/{id} ────

    /** Reconstitue l'occupation. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Reconstituer l'occupation d'un appartement")
    @ApiResponse(responseCode = "200", description = "Périodes d'occupation et de vacance")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Appartement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/occupation/{id}")
    public List<PeriodeOccupationResponse> occupation(@PathVariable @Positive Integer id) {
        return appartementService.findOccupation(id);
    }

    // ── create() — POST /gestion-locative/appartement/create ────────────────

    /** Crée un appartement. Réponses : 201 | 400 | 404 | 409 | 500. */
    @Operation(summary = "Créer un appartement")
    @ApiResponse(responseCode = "201", description = "Appartement créé")
    @ApiResponse(responseCode = "400", description = "Étage ou surface invalide")
    @ApiResponse(responseCode = "404", description = "Immeuble introuvable")
    @ApiResponse(responseCode = "409", description = "Numéro déjà utilisé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppartementResponse> create(
            @Valid @RequestBody CreateAppartementRequest request) {
        AppartementResponse created = appartementService.create(request);
        return ResponseEntity.created(
                URI.create("/gestion-locative/appartement/get/" + created.idAppartement()))
                .body(created);
    }

    // ── update() — PUT /gestion-locative/appartement/update/{id} ────────────

    /** Modifie un appartement. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Modifier un appartement")
    @ApiResponse(responseCode = "200", description = "Appartement modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Appartement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AppartementResponse update(@PathVariable @Positive Integer id,
                                      @Valid @RequestBody UpdateAppartementRequest request) {
        return appartementService.update(id, request);
    }

    // ── delete() — DELETE /gestion-locative/appartement/delete/{id} ─────────

    /** Supprime un appartement jamais loué. Réponses : 204 | 400 | 404 | 409 | 500. */
    @Operation(summary = "Supprimer un appartement")
    @ApiResponse(responseCode = "204", description = "Appartement supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Appartement introuvable")
    @ApiResponse(responseCode = "409", description = "Appartement déjà loué")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        appartementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

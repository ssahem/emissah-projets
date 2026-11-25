package com.billetterie.billetterie_api.controller;

import com.billetterie.billetterie_api.dto.request.CreatePlaceRequest;
import com.billetterie.billetterie_api.dto.request.UpdatePlaceRequest;
import com.billetterie.billetterie_api.dto.response.PlaceResponse;
import com.billetterie.billetterie_api.service.PlaceService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST des places physiques (F2).
 *
 * COUCHE       : Contrôleur
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /billetterie/place
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id}
 * NE FAIT PAS  : de règle métier.
 */
@Validated
@RestController
@RequestMapping(path = "/billetterie/place", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Places", description = "CRUD des places physiques d'une salle")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }
    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /billetterie/place/list ─────────────────────────────────

    /**
     * Liste toutes les places triées par rang puis par numéro. Réponses : 200 |
     * 500.
     */

    @Operation(summary = "Lister les places")
    @ApiResponse(responseCode = "200", description = "Places renvoyées")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<PlaceResponse> list() {
        return placeService.findAll();
    }

    // ── getById() — GET /billetterie/place/get/{id} ──────────────────────────

    /**
     * Consulte une place. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter une place")
    @ApiResponse(responseCode = "200", description = "Place renvoyée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Place introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public PlaceResponse getById(@PathVariable @Positive Integer id) {
        return placeService.findById(id);
    }

    // ── create() — POST /billetterie/place/create ────────────────────────────

    /**
     * Crée une place. Réponses : 201 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Créer une place")
    @ApiResponse(responseCode = "201", description = "Place créée")
    @ApiResponse(responseCode = "400", description = "Catégorie inconnue ou données invalides")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "409", description = "Place déjà existante ou capacité atteinte")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlaceResponse> create(@Valid @RequestBody CreatePlaceRequest request) {
        PlaceResponse created = placeService.create(request);
        return ResponseEntity.created(URI.create("/billetterie/place/get/" + created.idPlace()))
                .body(created);
    }

    // ── update() — PUT /billetterie/place/update/{id} ────────────────────────

    /**
     * Modifie une place ; la position est figée après réservation. Réponses :
     * 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Modifier une place")
    @ApiResponse(responseCode = "200", description = "Place modifiée")
    @ApiResponse(responseCode = "400", description = "Catégorie inconnue ou données invalides")
    @ApiResponse(responseCode = "404", description = "Place ou salle introuvable")
    @ApiResponse(responseCode = "409", description = "Position figée, doublon ou capacité atteinte")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlaceResponse update(@PathVariable @Positive Integer id,
                                @Valid @RequestBody UpdatePlaceRequest request) {
        return placeService.update(id, request);
    }

    // ── delete() — DELETE /billetterie/place/delete/{id} ─────────────────────

    /**
     * Supprime une place non réservée. Réponses : 204 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Supprimer une place")
    @ApiResponse(responseCode = "204", description = "Place supprimée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Place introuvable")
    @ApiResponse(responseCode = "409", description = "Place réservée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        placeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

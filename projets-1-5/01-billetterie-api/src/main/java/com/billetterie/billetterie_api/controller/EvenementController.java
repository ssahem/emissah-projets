package com.billetterie.billetterie_api.controller;

import com.billetterie.billetterie_api.dto.request.CreateEvenementRequest;
import com.billetterie.billetterie_api.dto.response.EvenementResponse;
import com.billetterie.billetterie_api.dto.response.PlacesLibresResponse;
import com.billetterie.billetterie_api.dto.response.RemplissageResponse;
import com.billetterie.billetterie_api.dto.request.UpdateEvenementRequest;
import com.billetterie.billetterie_api.service.EvenementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDateTime;
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
 * Controller REST des événements.
 *
 * COUCHE       : Contrôleur
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO ou du CSV.
 * URL DE BASE  : /billetterie/evenement
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id} · places-libres/{id} · remplissage/{id} · reservations/export/{id}
 * NE FAIT PAS  : de règle métier.
 */
@Validated
@RestController
@RequestMapping(path = "/billetterie/evenement", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Événements", description = "Gestion des événements et restitutions associées")
public class EvenementController {

    private final EvenementService evenementService;

    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }
    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /billetterie/evenement/list ─────────────────────────────

    /**
     * Liste les événements triés par date, avec un filtre de période optionnel
     * (F8). Réponses : 200 | 400 | 500.
     */

    @Operation(summary = "Lister les événements, éventuellement entre deux dates",
            description = "Les bornes du et au sont incluses, au format ISO-8601 DATE_TIME. Une seule borne est acceptée.")
    @ApiResponse(responseCode = "200", description = "Événements renvoyés")
    @ApiResponse(responseCode = "400", description = "Format de date invalide ou période inversée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<EvenementResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime au) {
        return evenementService.findBetween(du, au);
    }

    // ── getById() — GET /billetterie/evenement/get/{id} ──────────────────────

    /**
     * Consulte un événement. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter un événement")
    @ApiResponse(responseCode = "200", description = "Événement renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public EvenementResponse getById(@PathVariable @Positive Integer id) {
        return evenementService.findById(id);
    }

    // ── create() — POST /billetterie/evenement/create ────────────────────────

    /**
     * Crée un événement. Réponses : 201 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Créer un événement")
    @ApiResponse(responseCode = "201", description = "Événement créé")
    @ApiResponse(responseCode = "400", description = "Date passée ou données invalides")
    @ApiResponse(responseCode = "404", description = "Salle introuvable")
    @ApiResponse(responseCode = "409", description = "Salle sans place")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvenementResponse> create(@Valid @RequestBody CreateEvenementRequest request) {
        EvenementResponse created = evenementService.create(request);
        return ResponseEntity.created(URI.create("/billetterie/evenement/get/" + created.idEvenement()))
                .body(created);
    }

    // ── update() — PUT /billetterie/evenement/update/{id} ────────────────────

    /**
     * Modifie un événement. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Modifier un événement")
    @ApiResponse(responseCode = "200", description = "Événement modifié")
    @ApiResponse(responseCode = "400", description = "Date passée ou données invalides")
    @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    @ApiResponse(responseCode = "409", description = "Salle figée ou sans place")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EvenementResponse update(@PathVariable @Positive Integer id,
                                    @Valid @RequestBody UpdateEvenementRequest request) {
        return evenementService.update(id, request);
    }

    // ── delete() — DELETE /billetterie/evenement/delete/{id} ─────────────────

    /**
     * Supprime un événement sans réservation. Réponses : 204 | 400 | 404 | 409
     * | 500.
     */

    @Operation(summary = "Supprimer un événement")
    @ApiResponse(responseCode = "204", description = "Événement supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    @ApiResponse(responseCode = "409", description = "Réservations existantes")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        evenementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── placesLibres() — GET /billetterie/evenement/places-libres/{id} ───────

    /**
     * Retourne les places libres groupées par catégorie (F17). Réponses : 200 |
     * 400 | 404 | 500.
     */

    @Operation(summary = "Lister les places libres d'un événement")
    @ApiResponse(responseCode = "200", description = "Places libres renvoyées")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/places-libres/{id}")
    public PlacesLibresResponse placesLibres(@PathVariable @Positive Integer id) {
        return evenementService.findPlacesLibres(id);
    }

    // ── remplissage() — GET /billetterie/evenement/remplissage/{id} ──────────

    /**
     * Calcule le taux de remplissage d'un événement (F18). Réponses : 200 | 400
     * | 404 | 409 | 500.
     */

    @Operation(summary = "Calculer le taux de remplissage d'un événement",
            description = "Détail par catégorie ; les réservations annulées sont exclues.")
    @ApiResponse(responseCode = "200", description = "Taux de remplissage renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    @ApiResponse(responseCode = "409", description = "Salle sans place")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/remplissage/{id}")
    public RemplissageResponse remplissage(@PathVariable @Positive Integer id) {
        return evenementService.findRemplissage(id);
    }

    // ── exportReservations() — GET …terie/evenement/reservations/export/{id} ─

    /**
     * Exporte les réservations en CSV (F19). Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Exporter les réservations d'un événement en CSV")
    @ApiResponse(responseCode = "200", description = "Fichier CSV")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Événement introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping(path = "/reservations/export/{id}", produces = "text/csv")
    public String exportReservations(@PathVariable @Positive Integer id,
                                     @RequestParam(defaultValue = "false") boolean inclureAnnulees) {
        return evenementService.exportReservations(id, inclureAnnulees);
    }
}

package com.billetterie.billetterie_api.controller;

import com.billetterie.billetterie_api.dto.request.CreateReservationRequest;
import com.billetterie.billetterie_api.dto.response.AnnulationResponse;
import com.billetterie.billetterie_api.dto.response.ReservationResponse;
import com.billetterie.billetterie_api.dto.request.UpdateReservationRequest;
import com.billetterie.billetterie_api.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
 * Controller REST des réservations.
 *
 * COUCHE       : Contrôleur
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /billetterie/reservation
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id} · paiement/{id} · annulation/{id}
 * NE FAIT PAS  : de règle métier.
 */
@Validated
@RestController
@RequestMapping(path = "/billetterie/reservation", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Réservations", description = "Gestion des réservations et confirmation de paiement")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /billetterie/reservation/list ───────────────────────────

    /**
     * Liste les réservations par page, ou filtre par email (F9, F10). Réponses
     * : 200 | 400 | 500.
     */

    @Operation(summary = "Lister ou rechercher les réservations")
    @ApiResponse(responseCode = "200", description = "Réservations renvoyées")
    @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public Object list(
            @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) @Email String email) {
        if (email != null && !email.isBlank()) {
            return reservationService.findByEmail(email);
        }
        return reservationService.findAll(pageable);
    }

    // ── getById() — GET /billetterie/reservation/get/{id} ────────────────────

    /**
     * Consulte une réservation. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter une réservation")
    @ApiResponse(responseCode = "200", description = "Réservation renvoyée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public ReservationResponse getById(@PathVariable @Positive Integer id) {
        return reservationService.findById(id);
    }

    // ── create() — POST /billetterie/reservation/create ──────────────────────

    /**
     * Crée une réservation provisoire. Réponses : 201 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Créer une réservation")
    @ApiResponse(responseCode = "201", description = "Réservation créée")
    @ApiResponse(responseCode = "400", description = "Place ou événement incohérent")
    @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    @ApiResponse(responseCode = "409", description = "Place déjà réservée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse created = reservationService.create(request);
        return ResponseEntity.created(URI.create("/billetterie/reservation/get/" + created.idReservation()))
                .body(created);
    }

    // ── update() — PUT /billetterie/reservation/update/{id} ──────────────────

    /**
     * Modifie les informations client. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Modifier les informations client")
    @ApiResponse(responseCode = "200", description = "Réservation modifiée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReservationResponse update(@PathVariable @Positive Integer id,
                                      @Valid @RequestBody UpdateReservationRequest request) {
        return reservationService.update(id, request);
    }

    // ── delete() — DELETE /billetterie/reservation/delete/{id} ───────────────

    /**
     * Supprime une réservation provisoire ou annulée. Réponses : 204 | 400 |
     * 404 | 409 | 500.
     */

    @Operation(summary = "Supprimer une réservation")
    @ApiResponse(responseCode = "204", description = "Réservation supprimée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    @ApiResponse(responseCode = "409", description = "Réservation payée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── payer() — POST /billetterie/reservation/paiement/{id} ────────────────

    /**
     * Confirme le paiement (F14). Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Confirmer le paiement d'une réservation")
    @ApiResponse(responseCode = "200", description = "Réservation payée")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    @ApiResponse(responseCode = "409", description = "Statut incompatible ou délai dépassé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping("/paiement/{id}")
    public ReservationResponse payer(@PathVariable @Positive Integer id) {
        return reservationService.payer(id);
    }

    // ── annuler() — POST /billetterie/reservation/annulation/{id} ────────────

    /**
     * Annule une réservation et renvoie le montant remboursé (F15). Réponses :
     * 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Annuler une réservation et calculer le remboursement",
            description = "Remboursement intégral au-delà de 48 h, moitié entre 48 h et 24 h, nul en deçà.")
    @ApiResponse(responseCode = "200", description = "Réservation annulée et montant remboursé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    @ApiResponse(responseCode = "409", description = "Réservation déjà annulée ou statut incompatible")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping("/annulation/{id}")
    public AnnulationResponse annuler(@PathVariable @Positive Integer id) {
        return reservationService.annuler(id);
    }
}

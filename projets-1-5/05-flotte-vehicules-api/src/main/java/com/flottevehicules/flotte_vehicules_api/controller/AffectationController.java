package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.CreateRestitutionRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationResponse;
import com.flottevehicules.flotte_vehicules_api.service.AffectationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints des affectations : CRUD (F4), anti-chevauchement (F11),
 * immobilisation (F12) et restitution (F13).
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/affectation/... — action dans le chemin.
 * RÔLE         : Endpoints des affectations : CRUD (F4), anti-chevauchement (F11), immobilisation (F12) et restitution (F13).
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/affectation")
@Tag(name = "Affectation", description = "Affectations, conflits et restitutions")
public class AffectationController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final AffectationService service;

    public AffectationController(AffectationService service) {
        this.service = service;
    }

    /** F4, F11 et F12 : crée une affectation si le véhicule est libre. */

    // ── create() — POST /flotte-vehicules/affectation/create ─────────────────

    /**
     * Créer une affectation.
     */

    @PostMapping("/create")
    @Operation(summary = "Créer une affectation",
            description = "F4, F11 et F12 — refuse le chevauchement ou l'immobilisation")
    @ApiResponse(responseCode = "201", description = "Affectation créée")
    @ApiResponse(responseCode = "400", description = "Donnée invalide")
    @ApiResponse(responseCode = "409", description = "Véhicule déjà affecté ou immobilisé")
    public ResponseEntity<AffectationResponse> create(@Valid @RequestBody CreateAffectationRequest request) {
        AffectationResponse affectation = service.create(request);
        return ResponseEntity.created(
                URI.create("/flotte-vehicules/affectation/" + affectation.idAffectation()))
                .body(affectation);
    }

    /** F4 : liste triée par date de début décroissante. */

    // ── list() — GET /flotte-vehicules/affectation/list ──────────────────────

    /**
     * Lister les affectations.
     */

    @GetMapping("/list")
    @Operation(summary = "Lister les affectations")
    @ApiResponse(responseCode = "200", description = "Liste des affectations")
    public List<AffectationResponse> list() {
        return service.findAll();
    }

    /** F4 : détail d'une affectation. */

    // ── get() — GET /flotte-vehicules/affectation/{id} ───────────────────────

    /**
     * Consulter une affectation.
     */

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une affectation")
    @ApiResponse(responseCode = "200", description = "Affectation trouvée")
    @ApiResponse(responseCode = "404", description = "Affectation inconnue")
    public AffectationResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    /** F4 : modifie la date de fin, et les parties avant le début. */

    // ── update() — PUT /flotte-vehicules/affectation/{id}/update ─────────────

    /**
     * Modifier une affectation.
     */

    @PutMapping("/{id}/update")
    @Operation(summary = "Modifier une affectation", description = "Véhicule et salarié figés après le début")
    @ApiResponse(responseCode = "200", description = "Affectation modifiée")
    @ApiResponse(responseCode = "409", description = "Partie figée")
    public AffectationResponse update(@PathVariable Integer id,
                                      @Valid @RequestBody UpdateAffectationRequest request) {
        return service.update(id, request);
    }

    /** F4 : supprime une affectation qui n'a pas commencé. */

    // ── delete() — DELETE /flotte-vehicules/affectation/{id}/delete ──────────

    /**
     * Supprimer une affectation.
     */

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer une affectation", description = "Possible uniquement avant le début")
    @ApiResponse(responseCode = "204", description = "Affectation supprimée")
    @ApiResponse(responseCode = "409", description = "Affectation déjà commencée")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** F13 et F14 : clôture l'affectation avec date et kilométrage. */

    // ── restitution() — POST /flotte-vehicules/affectation/{id}/restitution ──

    /**
     * Restituer un véhicule.
     */

    @PostMapping("/{id}/restitution")
    @Operation(summary = "Restituer un véhicule", description = "F13 et F14 — date valide et compteur croissant")
    @ApiResponse(responseCode = "200", description = "Affectation clôturée")
    @ApiResponse(responseCode = "400", description = "Date ou kilométrage invalide")
    @ApiResponse(responseCode = "409", description = "Affectation déjà clôturée")
    public AffectationResponse restitution(@PathVariable Integer id,
                                           @Valid @RequestBody CreateRestitutionRequest request) {
        return service.restituer(id, request);
    }
}

package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.EntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.PageResponse;
import com.flottevehicules.flotte_vehicules_api.service.EntretienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
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
 * Endpoints des entretiens : CRUD (F5), historique (F8), pagination (F10) et alerte F12.
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/entretien/...
 * RÔLE         : Endpoints des entretiens : CRUD (F5), historique (F8), pagination (F10) et alerte F12.
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/entretien")
@Tag(name = "Entretien", description = "CRUD des entretiens, historique et pagination")
public class EntretienController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final EntretienService service;

    public EntretienController(EntretienService service) {
        this.service = service;
    }

    /** F5 : crée un entretien ; une alerte F12 est créée si une affectation existe. */

    // ── create() — POST /flotte-vehicules/entretien/create ───────────────────

    /**
     * Créer un entretien.
     */

    @PostMapping("/create")
    @Operation(summary = "Créer un entretien", description = "F5, F14 et F12 — coût positif et compteur croissant")
    @ApiResponse(responseCode = "201", description = "Entretien créé")
    @ApiResponse(responseCode = "400", description = "Donnée invalide")
    public ResponseEntity<EntretienResponse> create(@Valid @RequestBody CreateEntretienRequest request) {
        EntretienResponse entretien = service.create(request);
        return ResponseEntity.created(URI.create("/flotte-vehicules/entretien/" + entretien.idEntretien()))
                .body(entretien);
    }

    /** F10 : page d'entretiens. */

    // ── list() — GET /flotte-vehicules/entretien/list ────────────────────────

    /**
     * Lister les entretiens.
     */

    @GetMapping("/list")
    @Operation(summary = "Lister les entretiens", description = "F10 — page, taille plafonnée à 100 et tri")
    @ApiResponse(responseCode = "200", description = "Page d'entretiens")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide")
    public PageResponse<EntretienResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date,desc") String sort) {
        return service.findAll(page, size, sort);
    }

    /** F5 : détail d'un entretien. */

    // ── get() — GET /flotte-vehicules/entretien/{id} ─────────────────────────

    /**
     * Consulter un entretien.
     */

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un entretien")
    @ApiResponse(responseCode = "200", description = "Entretien trouvé")
    @ApiResponse(responseCode = "404", description = "Entretien inconnu")
    public EntretienResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    /** F5 : modifie coût et prestataire. */

    // ── update() — PUT /flotte-vehicules/entretien/{id}/update ───────────────

    /**
     * Modifier un entretien.
     */

    @PutMapping("/{id}/update")
    @Operation(summary = "Modifier un entretien", description = "Date et kilométrage figés")
    @ApiResponse(responseCode = "200", description = "Entretien modifié")
    @ApiResponse(responseCode = "409", description = "Date ou kilométrage figé")
    public EntretienResponse update(@PathVariable Integer id,
                                    @Valid @RequestBody UpdateEntretienRequest request) {
        return service.update(id, request);
    }

    /** F5 : suppression avant le début de l'immobilisation. */

    // ── delete() — DELETE /flotte-vehicules/entretien/{id}/delete ────────────

    /**
     * Supprimer un entretien.
     */

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer un entretien")
    @ApiResponse(responseCode = "204", description = "Entretien supprimé")
    @ApiResponse(responseCode = "409", description = "Immobilisation déjà commencée")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

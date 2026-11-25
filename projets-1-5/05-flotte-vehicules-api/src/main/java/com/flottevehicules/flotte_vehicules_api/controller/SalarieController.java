package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationSalarieResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.SalarieResponse;
import com.flottevehicules.flotte_vehicules_api.service.SalarieService;
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
 * Endpoints des fiches salarié (F3) et de leurs affectations (F7).
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/salarie/...
 * RÔLE         : Endpoints des fiches salarié (F3) et de leurs affectations (F7).
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/salarie")
@Tag(name = "Salarié", description = "CRUD des salariés et historique de leurs affectations (F7)")
public class SalarieController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final SalarieService service;

    public SalarieController(SalarieService service) {
        this.service = service;
    }


    // ── create() — POST /flotte-vehicules/salarie/create ─────────────────────

    /**
     * Créer un salarié.
     */

    @PostMapping("/create")
    @Operation(summary = "Créer un salarié", description = "Nom, matricule et service obligatoires")
    @ApiResponse(responseCode = "201", description = "Salarié créé")
    @ApiResponse(responseCode = "400", description = "Corps invalide")
    @ApiResponse(responseCode = "409", description = "Matricule ou email déjà utilisé")
    public ResponseEntity<SalarieResponse> create(@Valid @RequestBody CreateSalarieRequest request) {
        SalarieResponse salarie = service.create(request);
        return ResponseEntity.created(URI.create("/flotte-vehicules/salarie/" + salarie.idSalarie()))
                .body(salarie);
    }


    // ── list() — GET /flotte-vehicules/salarie/list ──────────────────────────

    /**
     * Lister les salariés.
     */

    @GetMapping("/list")
    @Operation(summary = "Lister les salariés", description = "Tri par nom croissant")
    @ApiResponse(responseCode = "200", description = "Liste des salariés")
    public List<SalarieResponse> list() {
        return service.findAll();
    }


    // ── get() — GET /flotte-vehicules/salarie/{id} ───────────────────────────

    /**
     * Consulter un salarié.
     */

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un salarié")
    @ApiResponse(responseCode = "200", description = "Salarié trouvé")
    @ApiResponse(responseCode = "404", description = "Salarié inconnu")
    public SalarieResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }


    // ── update() — PUT /flotte-vehicules/salarie/{id}/update ─────────────────

    /**
     * Modifier un salarié.
     */

    @PutMapping("/{id}/update")
    @Operation(summary = "Modifier un salarié", description = "Le matricule n'est pas modifiable")
    @ApiResponse(responseCode = "200", description = "Salarié modifié")
    @ApiResponse(responseCode = "404", description = "Salarié inconnu")
    public SalarieResponse update(@PathVariable Integer id,
                                  @Valid @RequestBody UpdateSalarieRequest request) {
        return service.update(id, request);
    }


    // ── delete() — DELETE /flotte-vehicules/salarie/{id}/delete ──────────────

    /**
     * Supprimer un salarié.
     */

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer un salarié")
    @ApiResponse(responseCode = "204", description = "Salarié supprimé")
    @ApiResponse(responseCode = "409", description = "Affectation en cours")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    // ── affectations() — GET /flotte-vehicules/salarie/{id}/affectations ─────

    /**
     * Affectations d'un salarié.
     */

    @GetMapping("/{id}/affectations")
    @Operation(summary = "Affectations d'un salarié", description = "F7 — l'affectation en cours est signalée")
    @ApiResponse(responseCode = "200", description = "Historique des affectations")
    @ApiResponse(responseCode = "404", description = "Salarié inconnu")
    public List<AffectationSalarieResponse> affectations(@PathVariable Integer id) {
        return service.affectations(id);
    }
}

package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.service.MatiereService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
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
 * Endpoints CRUD des matières (F3).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir les requêtes CRUD et déléguer les règles de coefficient et de
 *                suppression.
 * URL DE BASE  : /suivi-scolaire/matiere
 * ENDPOINTS    : create · list · get/{id} · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : MatiereService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/matiere")
@Tag(name="Matières", description="Gestion des matières enseignées (F3)")
public class MatiereController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final MatiereService service;

    /** Spring injecte le service par constructeur. */
    public MatiereController(MatiereService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /suivi-scolaire/matiere/list ────────────────────────────

    /**
     * Lister les matières.
     */

    @Operation(summary = "Lister les matières", description = "Triées par libellé (F3)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MatiereResponse> list() {
        return service.findAll();
    }

    // ── get() — GET /suivi-scolaire/matiere/get/{id} ─────────────────────────

    /**
     * Lire une matière.
     */

    @Operation(summary = "Lire une matière")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public MatiereResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    // ── create() — POST /suivi-scolaire/matiere/create ───────────────────────

    /**
     * Créer une matière.
     */

    @Operation(summary = "Créer une matière", description = "Libellé unique et coefficient positif (F3)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MatiereResponse> create(@Valid @RequestBody CreateMatiereRequest request) {
        MatiereResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/suivi-scolaire/matiere/get/" + created.idMatiere()))
                .body(created);
    }

    // ── update() — PUT /suivi-scolaire/matiere/update/{id} ───────────────────

    /**
     * Modifier une matière.
     */

    @Operation(summary = "Modifier une matière", description = "Coefficient figé après clôture d'un trimestre (F3)")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public MatiereResponse update(@PathVariable Integer id,
                                  @Valid @RequestBody UpdateMatiereRequest request) {
        return service.update(id, request);
    }

    // ── delete() — DELETE /suivi-scolaire/matiere/delete/{id} ────────────────

    /**
     * Supprimer une matière.
     */

    @Operation(summary = "Supprimer une matière", description = "Refusée si affectée ou notée (F3)")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

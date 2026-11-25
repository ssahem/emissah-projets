package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.AffectationMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateProfesseurRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateProfesseurRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.AffectationResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ProfesseurResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ProfesseurService;
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
 * Endpoints des professeurs et de leurs affectations (F4, F12).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : CRUD des professeurs et affectation d'une matière dans une classe.
 * URL DE BASE  : /suivi-scolaire/professeur
 * ENDPOINTS    : create · list · get/{id} · update/{id} · delete/{id} · affectations/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : ProfesseurService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/professeur")
@Tag(name="Professeurs", description="Professeurs et affectations (F4, F12)")
public class ProfesseurController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final ProfesseurService service;

    /** Spring injecte le service par constructeur. */
    public ProfesseurController(ProfesseurService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /suivi-scolaire/professeur/list ─────────────────────────

    /**
     * Lister les professeurs.
     */

    @Operation(summary = "Lister les professeurs", description = "Triés par nom puis prénom (F4)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProfesseurResponse> list() {
        return service.findAll();
    }

    // ── get() — GET /suivi-scolaire/professeur/get/{id} ──────────────────────

    /**
     * Lire un professeur.
     */

    @Operation(summary = "Lire un professeur")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfesseurResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    // ── create() — POST /suivi-scolaire/professeur/create ────────────────────

    /**
     * Créer un professeur.
     */

    @Operation(summary = "Créer un professeur",
            description = "Email unique ; la matière principale est retrouvée via enseignement (F4, A-02)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfesseurResponse> create(@Valid @RequestBody CreateProfesseurRequest request) {
        ProfesseurResponse created = service.create(request);
        return ResponseEntity.created(
                URI.create("/suivi-scolaire/professeur/get/" + created.idProfesseur())).body(created);
    }

    // ── update() — PUT /suivi-scolaire/professeur/update/{id} ────────────────

    /**
     * Modifier un professeur.
     */

    @Operation(summary = "Modifier un professeur", description = "Coordonnées et matière principale (F4)")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfesseurResponse update(@PathVariable Integer id,
                                     @Valid @RequestBody UpdateProfesseurRequest request) {
        return service.update(id, request);
    }

    // ── delete() — DELETE /suivi-scolaire/professeur/delete/{id} ─────────────

    /**
     * Supprimer un professeur.
     */

    @Operation(summary = "Supprimer un professeur", description = "Refusé s'il est affecté (F4)")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── affecter() — POST /suivi-scolaire/professeur/affectations/{id} ───────

    /**
     * Affecter une matière.
     */

    @Operation(summary = "Affecter une matière",
            description = "Un seul professeur par matière et par classe ; 409 citant le titulaire (F12)")
    @PostMapping(path = "/affectations/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AffectationResponse> affecter(
            @PathVariable Integer id, @Valid @RequestBody AffectationMatiereRequest request) {
        return ResponseEntity.status(201).body(service.affecter(id, request));
    }
}

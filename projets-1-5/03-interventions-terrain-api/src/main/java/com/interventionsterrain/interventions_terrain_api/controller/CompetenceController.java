package com.interventionsterrain.interventions_terrain_api.controller;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.service.CompetenceService;
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
 * Endpoints CRUD des compétences (F3).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir les requêtes HTTP de Competence, valider les paramètres et déléguer
 *                au service.
 * URL DE BASE  : /interventions-terrain/competence
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : CompetenceService
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/interventions-terrain/competence")
@Tag(name = "Compétences", description = "Gestion des compétences (F3)")
public class CompetenceController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final CompetenceService service;

    public CompetenceController(CompetenceService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Lister les compétences", description = "Triées par libellé")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompetenceResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Lire une compétence")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompetenceResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une compétence")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetenceResponse> create(@Valid @RequestBody CreateCompetenceRequest request) {
        CompetenceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/interventions-terrain/competence/get/" + created.idCompetence()))
                .body(created);
    }

    @Operation(summary = "Modifier une compétence")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CompetenceResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateCompetenceRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une compétence")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

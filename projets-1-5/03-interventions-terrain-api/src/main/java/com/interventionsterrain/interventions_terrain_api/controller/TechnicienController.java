package com.interventionsterrain.interventions_terrain_api.controller;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateObtentionCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.DisponiblesResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.PlanningInterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.service.RestitutionService;
import com.interventionsterrain.interventions_terrain_api.service.TechnicienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
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
 * Endpoints CRUD des techniciens (F2).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir les requêtes HTTP de Technicien, valider les paramètres et déléguer
 *                au service.
 * URL DE BASE  : /interventions-terrain/technicien
 * ENDPOINTS    : list · get/{id} · competences/{id} · planning/{id} · disponibles
 *                competence/{id} · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : RestitutionService, TechnicienService
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/interventions-terrain/technicien")
@Tag(name = "Techniciens", description = "Gestion des techniciens (F2)")
public class TechnicienController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final TechnicienService service;
    private final RestitutionService restitutionService;

    public TechnicienController(TechnicienService service, RestitutionService restitutionService) {
        this.service = service;
        this.restitutionService = restitutionService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Lister les techniciens", description = "Triés par nom")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TechnicienResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Lire un technicien")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnicienResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Lister les compétences d'un technicien",
            description = "Compétences expirées signalées, jamais masquées (F7)")
    @GetMapping(path = "/competences/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompetenceTechnicienResponse> competences(@PathVariable Integer id) {
        return service.findCompetences(id);
    }

    @Operation(summary = "Consulter le planning d'un technicien",
            description = "Semaine courante par défaut, trois mois maximum (F10)")
    @GetMapping(path = "/planning/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlanningInterventionResponse> planning(
            @PathVariable Integer id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return service.findPlanning(id, du, au);
    }

    @Operation(summary = "Trouver les techniciens disponibles et compétents",
            description = "Créneau libre, compétences exigées non expirées, charge du jour par agence (F17)")
    @GetMapping(path = "/disponibles", produces = MediaType.APPLICATION_JSON_VALUE)
    public DisponiblesResponse disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam String type) {
        return restitutionService.findDisponibles(debut, fin, type);
    }

    @Operation(summary = "Associer une compétence à un technicien",
            description = "Dates cohérentes et aucun doublon actif (F11)")
    @PostMapping(path = "/competence/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetenceTechnicienResponse> associerCompetence(
            @PathVariable Integer id, @Valid @RequestBody CreateObtentionCompetenceRequest request) {
        return ResponseEntity.status(201).body(service.associerCompetence(id, request));
    }

    @Operation(summary = "Créer un technicien", description = "L'agence est déduite de l'équipe (A-01)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TechnicienResponse> create(@Valid @RequestBody CreateTechnicienRequest request) {
        TechnicienResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/interventions-terrain/technicien/get/" + created.idTechnicien()))
                .body(created);
    }

    @Operation(summary = "Modifier un technicien")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnicienResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateTechnicienRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer un technicien")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

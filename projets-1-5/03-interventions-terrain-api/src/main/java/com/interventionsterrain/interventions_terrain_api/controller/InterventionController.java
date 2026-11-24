package com.interventionsterrain.interventions_terrain_api.controller;

import com.interventionsterrain.interventions_terrain_api.dto.request.AddAffectationRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateRepriseRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateStatutRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AffectationResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.service.InterventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
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
 * Endpoints CRUD des interventions et de l'affectation (F5, A-02).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir les requêtes HTTP de Intervention, valider les paramètres et
 *                déléguer au service.
 * URL DE BASE  : /interventions-terrain/intervention
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id} · affectation/{id}
 *                affectation/{id} · affectation/{id}/{idTechnicien} · statut/{id}
 *                reprise/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : InterventionService
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/interventions-terrain/intervention")
@Tag(name = "Interventions", description = "Gestion des interventions (F5)")
public class InterventionController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final InterventionService service;

    public InterventionController(InterventionService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Filtrer les interventions",
            description = "Statut et période optionnels et combinables (F9), triées par début prévu")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InterventionResponse> list(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return service.findAll(statut, du, au);
    }

    @Operation(summary = "Lire une intervention")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public InterventionResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une intervention",
            description = "Crée l'adresse, l'affectation responsable et le statut PLANIFIEE (A-03, E-21)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InterventionResponse> create(@Valid @RequestBody CreateInterventionRequest request) {
        InterventionResponse created = service.create(request);
        return ResponseEntity.created(
                URI.create("/interventions-terrain/intervention/get/" + created.idIntervention()))
                .body(created);
    }

    @Operation(summary = "Modifier une intervention",
            description = "Créneau et responsable ; le statut courant est recopié en cas de changement")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterventionResponse update(@PathVariable Integer id,
                                       @Valid @RequestBody UpdateInterventionRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une intervention")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ajouter un technicien à une intervention",
            description = "estResponsable=true promu le technicien ; un second responsable renvoie 409 (A-02)")
    @PostMapping(path = "/affectation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AffectationResponse> addAffectation(
            @PathVariable Integer id, @Valid @RequestBody AddAffectationRequest request) {
        return ResponseEntity.status(201).body(service.addAffectation(id, request));
    }

    @Operation(summary = "Lister les affectations d'une intervention")
    @GetMapping(path = "/affectation/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AffectationResponse> affectations(@PathVariable Integer id) {
        return service.findAffectations(id);
    }

    @Operation(summary = "Retirer un technicien non responsable",
            description = "Le responsable se remplace via PUT update/{id}")
    @DeleteMapping(path = "/affectation/{id}/{idTechnicien}")
    public ResponseEntity<Void> removeAffectation(@PathVariable Integer id,
                                                   @PathVariable Integer idTechnicien) {
        service.removeAffectation(id, idTechnicien);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Faire évoluer le statut",
            description = "Transitions autorisées uniquement, horodatées sur le responsable (F14)")
    @PostMapping(path = "/statut/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterventionResponse changerStatut(@PathVariable Integer id,
                                              @Valid @RequestBody UpdateStatutRequest request) {
        return service.changerStatut(id, request);
    }

    @Operation(summary = "Reprogrammer une intervention échouée",
            description = "Crée une nouvelle intervention PLANIFIEE liée à l'originale (F15)")
    @PostMapping(path = "/reprise/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InterventionResponse> creerReprise(
            @PathVariable Integer id, @Valid @RequestBody CreateRepriseRequest request) {
        InterventionResponse reprise = service.creerReprise(id, request);
        return ResponseEntity.created(
                URI.create("/interventions-terrain/intervention/get/" + reprise.idIntervention()))
                .body(reprise);
    }
}

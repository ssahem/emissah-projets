package com.interventionsterrain.interventions_terrain_api.controller;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxReussiteResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienEffectifResponse;
import com.interventionsterrain.interventions_terrain_api.service.AgenceService;
import com.interventionsterrain.interventions_terrain_api.service.RestitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
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

/** Endpoints CRUD des agences (F1). */
@RestController
@RequestMapping("/interventions-terrain/agence")
@Tag(name = "Agences", description = "Gestion des agences (F1)")
public class AgenceController {

    private final AgenceService service;
    private final RestitutionService restitutionService;

    public AgenceController(AgenceService service, RestitutionService restitutionService) {
        this.service = service;
        this.restitutionService = restitutionService;
    }

    @Operation(summary = "Lister les agences", description = "Triées par ville puis par nom")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AgenceResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Lire une agence")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AgenceResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Lister les techniciens d'une agence",
            description = "Résolution via equipe.id_agence et compétences valides (F6)")
    @GetMapping(path = "/techniciens/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TechnicienEffectifResponse> techniciens(@PathVariable Integer id) {
        return service.findTechniciens(id);
    }

    @Operation(summary = "Mesurer la charge d'une agence",
            description = "Détail par technicien et totaux, annulations exclues (F18)")
    @GetMapping(path = "/charge/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChargeAgenceResponse charge(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime au) {
        return restitutionService.findCharge(id, du, au);
    }

    @Operation(summary = "Taux de réussite mensuel d'une agence",
            description = "TERMINEE / (TERMINEE + ECHOUEE), global et par type (F19)")
    @GetMapping(path = "/taux-reussite/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TauxReussiteResponse tauxReussite(@PathVariable Integer id,
                                             @RequestParam String mois) {
        return restitutionService.findTauxReussite(id, mois);
    }

    @Operation(summary = "Créer une agence")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgenceResponse> create(@Valid @RequestBody CreateAgenceRequest request) {
        AgenceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/interventions-terrain/agence/get/" + created.idAgence()))
                .body(created);
    }

    @Operation(summary = "Modifier une agence")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AgenceResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateAgenceRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une agence")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

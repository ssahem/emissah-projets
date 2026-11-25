package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.ChangeClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ChangementClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EleveResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyenneGeneraleResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MoyennesResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesMatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ConsultationService;
import com.suiviscolaire.suivi_scolaire_api.service.EleveService;
import com.suiviscolaire.suivi_scolaire_api.service.RestitutionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints des élèves, de leurs notes, de leur affectation et de leurs moyennes (F2, F7,
 *   F9, F11, F17, F18).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : CRUD des élèves, recherche, notes, changement de classe et restitution des
 *                moyennes.
 * URL DE BASE  : /suivi-scolaire/eleve
 * ENDPOINTS    : create · list · get/{id} · update/{id} · delete/{id} · notes/{id} ·
 *                affectation/{id} · moyennes/{id} · moyenne-generale/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : EleveService, ConsultationService, RestitutionService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/eleve")
@Tag(name = "Élèves", description="Élèves, recherches, notes et moyennes (F2, F7, F9, F11, F17, F18)")
public class EleveController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final EleveService service;
    private final ConsultationService consultationService;
    private final RestitutionService restitutionService;

    /** Spring injecte les services par constructeur. */
    public EleveController(EleveService service, ConsultationService consultationService,
                           RestitutionService restitutionService) {
        this.service = service;
        this.consultationService = consultationService;
        this.restitutionService = restitutionService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /suivi-scolaire/eleve/list ──────────────────────────────

    /**
     * Lister les élèves.
     */

    @Operation(summary = "Lister les élèves",
            description = "Triés par nom puis prénom ; ?nom=… déclenche la recherche partielle (F2, F9)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EleveResponse> list(@RequestParam(required = false) String nom) {
        return nom == null || nom.isBlank() ? service.findAll() : consultationService.rechercher(nom);
    }

    // ── get() — GET /suivi-scolaire/eleve/get/{id} ───────────────────────────

    /**
     * Lire un élève.
     */

    @Operation(summary = "Lire un élève")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EleveResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    // ── create() — POST /suivi-scolaire/eleve/create ─────────────────────────

    /**
     * Créer un élève.
     */

    @Operation(summary = "Créer un élève",
            description = "Date de naissance passée et classe non pleine (F2)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EleveResponse> create(@Valid @RequestBody CreateEleveRequest request) {
        EleveResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/suivi-scolaire/eleve/get/" + created.idEleve()))
                .body(created);
    }

    // ── update() — PUT /suivi-scolaire/eleve/update/{id} ─────────────────────

    /**
     * Modifier un élève.
     */

    @Operation(summary = "Modifier un élève", description = "Date de naissance figée ; classe modifiable (F2)")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public EleveResponse update(@PathVariable Integer id,
                                @Valid @RequestBody UpdateEleveRequest request) {
        return service.update(id, request);
    }

    // ── delete() — DELETE /suivi-scolaire/eleve/delete/{id} ──────────────────

    /**
     * Supprimer un élève.
     */

    @Operation(summary = "Supprimer un élève", description = "Refusée s'il possède des notes (F2)")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── notes() — GET /suivi-scolaire/eleve/notes/{id} ───────────────────────

    /**
     * Notes d'un élève.
     */

    @Operation(summary = "Notes d'un élève",
            description = "Groupées par matière ; sans trimestre, toutes les notes (F7)")
    @GetMapping(path = "/notes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NotesMatiereResponse> notes(@PathVariable Integer id,
                                            @RequestParam(required = false) Integer trimestre) {
        return consultationService.notesEleve(id, trimestre);
    }

    // ── changerClasse() — POST /suivi-scolaire/eleve/affectation/{id} ────────

    /**
     * Changer un élève de classe.
     */

    @Operation(summary = "Changer un élève de classe",
            description = "Même niveau, classe non pleine, ancienne affectation conservée (F11)")
    @PostMapping(path = "/affectation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChangementClasseResponse> changerClasse(
            @PathVariable Integer id, @Valid @RequestBody ChangeClasseRequest request) {
        return ResponseEntity.status(201).body(service.changerClasse(id, request));
    }

    // ── moyennes() — GET /suivi-scolaire/eleve/moyennes/{id} ─────────────────

    /**
     * Moyennes par matière.
     */

    @Operation(summary = "Moyennes par matière",
            description = "Pondérées par le poids du type, avec nombre de notes et non-évaluées (F17)")
    @GetMapping(path = "/moyennes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public MoyennesResponse moyennes(@PathVariable Integer id, @RequestParam Integer trimestre) {
        return restitutionService.moyennesParMatiere(id, trimestre);
    }

    // ── moyenneGenerale() — GET /suivi-scolaire/eleve/moyenne-generale/{id} ──

    /**
     * Moyenne générale.
     */

    @Operation(summary = "Moyenne générale",
            description = "Pondérée par les coefficients avec détail ; absente si aucune note (F18)")
    @GetMapping(path = "/moyenne-generale/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public MoyenneGeneraleResponse moyenneGenerale(@PathVariable Integer id,
                                                   @RequestParam Integer trimestre) {
        return restitutionService.moyenneGenerale(id, trimestre);
    }
}

package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NoteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PageResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ConsultationService;
import com.suiviscolaire.suivi_scolaire_api.service.NoteService;
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
 * Endpoints des notes et de leur pagination (F5, F10).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : CRUD des notes ; GET /list renvoie les notes groupées par matière (F5) ou une
 *                page dès qu'un paramètre page, size ou sort est fourni (F10).
 * URL DE BASE  : /suivi-scolaire/note
 * ENDPOINTS    : create · list · get/{id} · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : NoteService, ConsultationService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/note")
@Tag(name = "Notes", description="Saisie, consultation et pagination des notes (F5, F10)")
public class NoteController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final NoteService service;
    private final ConsultationService consultationService;

    /** Spring injecte les services par constructeur. */
    public NoteController(NoteService service, ConsultationService consultationService) {
        this.service = service;
        this.consultationService = consultationService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Lister les notes",
            description = "Groupées par matière ; avec ?page=&size=&sort=valeur,desc : page de notes (F5, F10)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object list(@RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false) String sort) {
        if (page == null && size == null && sort == null) {
            return service.findAll();
        }
        return consultationService.notesPaginees(
                page == null ? 0 : page,
                size == null ? 30 : size,
                sort == null ? "idNote,asc" : sort);
    }

    @Operation(summary = "Lire une note")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une note",
            description = "Valeur entre 0 et 20, trimestre ouvert, élève et matière résolus en service (F5)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        NoteResponse created = service.create(request);
        return ResponseEntity.created(service.location(created.idNote())).body(created);
    }

    @Operation(summary = "Modifier une note", description = "Seule la valeur est modifiable ; trimestre ouvert (F5)")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateNoteRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une note", description = "Possible seulement si le trimestre est ouvert (F5)")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

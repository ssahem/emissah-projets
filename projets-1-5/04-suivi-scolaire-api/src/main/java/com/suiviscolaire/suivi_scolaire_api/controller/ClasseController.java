package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClassementResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EffectifClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ClasseService;
import com.suiviscolaire.suivi_scolaire_api.service.ConsultationService;
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
 * Endpoints des classes, de leur effectif et du classement (F1, F6, F8, F19).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : CRUD des classes, élèves d'une classe, notes d'une matière et classement.
 * URL DE BASE  : /suivi-scolaire/classe
 * ENDPOINTS    : create · list · get/{id} · update/{id} · delete/{id} · eleves/{id} ·
 *                notes/{id} · classement/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : ClasseService, ConsultationService, RestitutionService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/classe")
@Tag(name = "Classes", description="Gestion des classes, effectifs et classements (F1, F6, F8, F19)")
public class ClasseController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final ClasseService service;
    private final ConsultationService consultationService;
    private final RestitutionService restitutionService;

    /** Spring injecte les services par constructeur. */
    public ClasseController(ClasseService service, ConsultationService consultationService,
                            RestitutionService restitutionService) {
        this.service = service;
        this.consultationService = consultationService;
        this.restitutionService = restitutionService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Lister les classes", description = "Triées par niveau puis par nom, avec effectif actuel (F1)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ClasseResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Lire une classe")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClasseResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une classe", description = "Nom unique dans le niveau, effectif positif (F1)")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClasseResponse> create(@Valid @RequestBody CreateClasseRequest request) {
        ClasseResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/suivi-scolaire/classe/get/" + created.idClasse()))
                .body(created);
    }

    @Operation(summary = "Modifier une classe", description = "Effectif maximal jamais sous l'effectif actuel (F1)")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ClasseResponse update(@PathVariable Integer id,
                                 @Valid @RequestBody UpdateClasseRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une classe", description = "Refusée si des élèves y sont inscrits (F1)")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Élèves d'une classe", description = "Triés par nom puis prénom, avec effectifs (F6)")
    @GetMapping(path = "/eleves/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EffectifClasseResponse eleves(@PathVariable Integer id) {
        return consultationService.elevesDeClasse(id);
    }

    @Operation(summary = "Notes d'une matière dans une classe",
            description = "Groupées par élève avec moyenne de classe ; 400 si matière non enseignée (F8)")
    @GetMapping(path = "/notes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NotesClasseResponse notes(@PathVariable Integer id,
                                     @RequestParam Integer matiere,
                                     @RequestParam Integer trimestre) {
        return consultationService.notesMatiereClasse(id, matiere, trimestre);
    }

    @Operation(summary = "Classement d'une classe",
            description = "Rangs avec ex æquo ; élèves sans note en fin de liste sans rang (F19)")
    @GetMapping(path = "/classement/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClassementResponse classement(@PathVariable Integer id, @RequestParam Integer trimestre) {
        return restitutionService.classement(id, trimestre);
    }
}

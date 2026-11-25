package com.gestionlocative.gestion_locative_api.controller;

import com.gestionlocative.gestion_locative_api.dto.request.CreateLocataireRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateLocataireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.LocataireResponse;
import com.gestionlocative.gestion_locative_api.service.LocataireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST des locataires (F4).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /gestion-locative/locataire
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique documentée dans le README.
 * DÉPEND DE    : LocataireService uniquement.
 * NE FAIT PAS  : de logique métier, d'accès base, de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/gestion-locative/locataire", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Locataires", description = "Gestion des fiches locataires (F4)")
public class LocataireController {

    private final LocataireService locataireService;

    /** Injection du service par Spring. */
    public LocataireController(LocataireService locataireService) {
        this.locataireService = locataireService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /gestion-locative/locataire/list ────────────────────────

    /** Liste les locataires triés par nom. Réponses : 200 | 500. */
    @Operation(summary = "Lister les locataires")
    @ApiResponse(responseCode = "200", description = "Locataires renvoyés")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<LocataireResponse> list() {
        return locataireService.findAll();
    }

    // ── getById() — GET /gestion-locative/locataire/get/{id} ─────────────────

    /** Consulte un locataire. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Consulter un locataire")
    @ApiResponse(responseCode = "200", description = "Locataire renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Locataire introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public LocataireResponse getById(@PathVariable @Positive Integer id) {
        return locataireService.findById(id);
    }

    // ── create() — POST /gestion-locative/locataire/create ───────────────────

    /** Crée un locataire. Réponses : 201 | 400 | 409 | 500. */
    @Operation(summary = "Créer un locataire")
    @ApiResponse(responseCode = "201", description = "Locataire créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocataireResponse> create(@Valid @RequestBody CreateLocataireRequest request) {
        LocataireResponse created = locataireService.create(request);
        return ResponseEntity.created(
                URI.create("/gestion-locative/locataire/get/" + created.idLocataire()))
                .body(created);
    }

    // ── update() — PUT /gestion-locative/locataire/update/{id} ───────────────

    /** Modifie un locataire. Réponses : 200 | 400 | 404 | 409 | 500. */
    @Operation(summary = "Modifier un locataire")
    @ApiResponse(responseCode = "200", description = "Locataire modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Locataire introuvable")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LocataireResponse update(@PathVariable @Positive Integer id,
                                    @Valid @RequestBody UpdateLocataireRequest request) {
        return locataireService.update(id, request);
    }

    // ── delete() — DELETE /gestion-locative/locataire/delete/{id} ────────────

    /** Supprime un locataire sans bail en cours. Réponses : 204 | 400 | 404 | 409 | 500. */
    @Operation(summary = "Supprimer un locataire")
    @ApiResponse(responseCode = "204", description = "Locataire supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Locataire introuvable")
    @ApiResponse(responseCode = "409", description = "Locataire encore titulaire d'un bail")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        locataireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.gestionlocative.gestion_locative_api.controller;

import com.gestionlocative.gestion_locative_api.dto.request.CreateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleProprietaireResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ProprietaireResponse;
import com.gestionlocative.gestion_locative_api.service.ProprietaireService;
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
 * Contrôleur REST des propriétaires (F1).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /gestion-locative/proprietaire
 * ENDPOINTS    : list · get/{id} · immeubles/{id} · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique documentée dans le README.
 * DÉPEND DE    : ProprietaireService uniquement.
 * NE FAIT PAS  : de logique métier, d'accès base, de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/gestion-locative/proprietaire", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Propriétaires", description = "Gestion des fiches propriétaires (F1)")
public class ProprietaireController {

    private final ProprietaireService proprietaireService;

    /** Injection du service par Spring. */
    public ProprietaireController(ProprietaireService proprietaireService) {
        this.proprietaireService = proprietaireService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /gestion-locative/proprietaire/list ─────────────────────

    /**
     * Liste les propriétaires triés par nom. Réponses : 200 | 500.
     */

    @Operation(summary = "Lister les propriétaires")
    @ApiResponse(responseCode = "200", description = "Propriétaires renvoyés")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<ProprietaireResponse> list() {
        return proprietaireService.findAll();
    }

    // ── getById() — GET /gestion-locative/proprietaire/get/{id} ──────────────

    /**
     * Consulte un propriétaire. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter un propriétaire")
    @ApiResponse(responseCode = "200", description = "Propriétaire renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Propriétaire introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public ProprietaireResponse getById(@PathVariable @Positive Integer id) {
        return proprietaireService.findById(id);
    }

    // ── immeubles() — GET /gestion-locative/proprietaire/immeubles/{id} ──────

    /**
     * Liste les immeubles avec leurs compteurs. Réponses : 200 | 400 | 404 |
     * 500.
     */

    @Operation(summary = "Lister les immeubles d'un propriétaire")
    @ApiResponse(responseCode = "200", description = "Immeubles renvoyés")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Propriétaire introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/immeubles/{id}")
    public List<ImmeubleProprietaireResponse> immeubles(@PathVariable @Positive Integer id) {
        return proprietaireService.findImmeubles(id);
    }

    // ── create() — POST /gestion-locative/proprietaire/create ────────────────

    /**
     * Crée un propriétaire. Réponses : 201 | 400 | 409 | 500.
     */

    @Operation(summary = "Créer un propriétaire")
    @ApiResponse(responseCode = "201", description = "Propriétaire créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProprietaireResponse> create(
            @Valid @RequestBody CreateProprietaireRequest request) {
        ProprietaireResponse created = proprietaireService.create(request);
        return ResponseEntity.created(
                URI.create("/gestion-locative/proprietaire/get/" + created.idProprietaire()))
                .body(created);
    }

    // ── update() — PUT /gestion-locative/proprietaire/update/{id} ────────────

    /**
     * Modifie un propriétaire. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Modifier un propriétaire")
    @ApiResponse(responseCode = "200", description = "Propriétaire modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Propriétaire introuvable")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProprietaireResponse update(@PathVariable @Positive Integer id,
                                       @Valid @RequestBody UpdateProprietaireRequest request) {
        return proprietaireService.update(id, request);
    }

    // ── delete() — DELETE /gestion-locative/proprietaire/delete/{id} ─────────

    /**
     * Supprime un propriétaire sans immeuble. Réponses : 204 | 400 | 404 | 409
     * | 500.
     */

    @Operation(summary = "Supprimer un propriétaire")
    @ApiResponse(responseCode = "204", description = "Propriétaire supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Propriétaire introuvable")
    @ApiResponse(responseCode = "409", description = "Propriétaire encore rattaché à des immeubles")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        proprietaireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

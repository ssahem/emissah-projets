package com.gestionlocative.gestion_locative_api.controller;

import com.gestionlocative.gestion_locative_api.dto.request.CreateImmeubleRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateImmeubleRequest;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementOccupationResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleResponse;
import com.gestionlocative.gestion_locative_api.dto.response.RevenuImmeubleResponse;
import com.gestionlocative.gestion_locative_api.service.ImmeubleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST des immeubles (F2).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /gestion-locative/immeuble
 * ENDPOINTS    : list · get/{id} · appartements/{id} · revenu/{id} (F19)
 *                · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique documentée dans le README.
 * DÉPEND DE    : ImmeubleService uniquement.
 * NE FAIT PAS  : de logique métier, d'accès base, de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/gestion-locative/immeuble", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Immeubles", description = "Gestion du parc immobilier (F2)")
public class ImmeubleController {

    private final ImmeubleService immeubleService;

    /** Injection du service par Spring. */
    public ImmeubleController(ImmeubleService immeubleService) {
        this.immeubleService = immeubleService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /gestion-locative/immeuble/list ─────────────────────────

    /** Liste les immeubles triés par adresse. Réponses : 200 | 500. */
    @Operation(summary = "Lister les immeubles")
    @ApiResponse(responseCode = "200", description = "Immeubles renvoyés")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public List<ImmeubleResponse> list() {
        return immeubleService.findAll();
    }

    // ── getById() — GET /gestion-locative/immeuble/get/{id} ──────────────────

    /** Consulte un immeuble. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Consulter un immeuble")
    @ApiResponse(responseCode = "200", description = "Immeuble renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Immeuble introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public ImmeubleResponse getById(@PathVariable @Positive Integer id) {
        return immeubleService.findById(id);
    }

    // ── appartements() — GET /gestion-locative/immeuble/appartements/{id} ────

    /** Liste les appartements avec leur occupation. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Lister les appartements d'un immeuble")
    @ApiResponse(responseCode = "200", description = "Appartements renvoyés")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Immeuble introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/appartements/{id}")
    public List<AppartementOccupationResponse> appartements(@PathVariable @Positive Integer id) {
        return immeubleService.findAppartements(id);
    }

    // ── revenu() — GET /gestion-locative/immeuble/revenu/{id} ────────────────

    /** Calcule le revenu du mois. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Calculer le revenu locatif d'un immeuble sur un mois")
    @ApiResponse(responseCode = "200", description = "Revenu total, détail et occupation")
    @ApiResponse(responseCode = "400", description = "Mois ou identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Immeuble introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/revenu/{id}")
    public RevenuImmeubleResponse revenu(@PathVariable @Positive Integer id,
                                         @RequestParam String mois) {
        return immeubleService.calculerRevenu(id, mois);
    }

    // ── create() — POST /gestion-locative/immeuble/create ────────────────────

    /** Crée un immeuble. Réponses : 201 | 400 | 404 | 500. */
    @Operation(summary = "Créer un immeuble")
    @ApiResponse(responseCode = "201", description = "Immeuble créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Propriétaire introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImmeubleResponse> create(@Valid @RequestBody CreateImmeubleRequest request) {
        ImmeubleResponse created = immeubleService.create(request);
        return ResponseEntity.created(URI.create("/gestion-locative/immeuble/get/" + created.idImmeuble()))
                .body(created);
    }

    // ── update() — PUT /gestion-locative/immeuble/update/{id} ────────────────

    /** Modifie un immeuble. Réponses : 200 | 400 | 404 | 500. */
    @Operation(summary = "Modifier un immeuble")
    @ApiResponse(responseCode = "200", description = "Immeuble modifié")
    @ApiResponse(responseCode = "400", description = "Nombre d'étages insuffisant")
    @ApiResponse(responseCode = "404", description = "Immeuble ou propriétaire introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ImmeubleResponse update(@PathVariable @Positive Integer id,
                                   @Valid @RequestBody UpdateImmeubleRequest request) {
        return immeubleService.update(id, request);
    }

    // ── delete() — DELETE /gestion-locative/immeuble/delete/{id} ─────────────

    /** Supprime un immeuble vide. Réponses : 204 | 400 | 404 | 409 | 500. */
    @Operation(summary = "Supprimer un immeuble")
    @ApiResponse(responseCode = "204", description = "Immeuble supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Immeuble introuvable")
    @ApiResponse(responseCode = "409", description = "Immeuble encore occupé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        immeubleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

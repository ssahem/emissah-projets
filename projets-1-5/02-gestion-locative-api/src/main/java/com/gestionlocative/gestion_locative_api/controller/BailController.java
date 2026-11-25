package com.gestionlocative.gestion_locative_api.controller;

import com.gestionlocative.gestion_locative_api.dto.request.CreateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RenouvellementBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.ResiliationBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RevisionLoyerRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.response.BailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PageResponse;
import com.gestionlocative.gestion_locative_api.service.BailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
 * Contrôleur REST des baux (F5, F10, F13 à F15).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : valider le format, déléguer au service, renvoyer des DTO.
 * URL DE BASE  : /gestion-locative/bail
 * ENDPOINTS    : list · get/{id} · create · update/{id} · delete/{id}
 *                · resiliation/{id} · renouvellement/{id} · revision/{id}
 * SÉCURITÉ     : aucune authentification — API publique documentée dans le README.
 * DÉPEND DE    : BailService uniquement.
 * NE FAIT PAS  : de logique métier, d'accès base, de try/catch.
 */
@Validated
@RestController
@RequestMapping(path = "/gestion-locative/bail", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Baux", description = "Gestion des baux (F5)")
public class BailController {

    private final BailService bailService;

    /** Injection du service par Spring. */
    public BailController(BailService bailService) {
        this.bailService = bailService;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /gestion-locative/bail/list ─────────────────────────────

    /**
     * Liste les baux par page. Réponses : 200 | 400 | 500.
     */

    @Operation(summary = "Lister les baux par page")
    @ApiResponse(responseCode = "200", description = "Page de baux renvoyée")
    @ApiResponse(responseCode = "400", description = "Paramètres de pagination invalides")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/list")
    public PageResponse<BailResponse> list(
            @PageableDefault(size = 20, sort = "dateDebut", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return bailService.findAll(pageable);
    }

    // ── getById() — GET /gestion-locative/bail/get/{id} ──────────────────────

    /**
     * Consulte un bail. Réponses : 200 | 400 | 404 | 500.
     */

    @Operation(summary = "Consulter un bail")
    @ApiResponse(responseCode = "200", description = "Bail renvoyé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @GetMapping("/get/{id}")
    public BailResponse getById(@PathVariable @Positive Integer id) {
        return bailService.findById(id);
    }

    // ── create() — POST /gestion-locative/bail/create ────────────────────────

    /**
     * Crée un bail sur un appartement libre. Réponses : 201 | 400 | 404 | 409 |
     * 500.
     */

    @Operation(summary = "Créer un bail")
    @ApiResponse(responseCode = "201", description = "Bail créé")
    @ApiResponse(responseCode = "400", description = "Dates incohérentes")
    @ApiResponse(responseCode = "404", description = "Appartement ou locataire introuvable")
    @ApiResponse(responseCode = "409", description = "Appartement déjà loué sur la période")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BailResponse> create(@Valid @RequestBody CreateBailRequest request) {
        BailResponse created = bailService.create(request);
        return ResponseEntity.created(URI.create("/gestion-locative/bail/get/" + created.idBail()))
                .body(created);
    }

    // ── update() — PUT /gestion-locative/bail/update/{id} ────────────────────

    /**
     * Modifie un bail. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Modifier un bail")
    @ApiResponse(responseCode = "200", description = "Bail modifié")
    @ApiResponse(responseCode = "400", description = "Date de fin invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "409", description = "Loyer d'un bail commencé non modifiable")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BailResponse update(@PathVariable @Positive Integer id,
                               @Valid @RequestBody UpdateBailRequest request) {
        return bailService.update(id, request);
    }

    // ── resilier() — POST /gestion-locative/bail/resiliation/{id} ────────────

    /**
     * Résilie un bail. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Résilier un bail par anticipation")
    @ApiResponse(responseCode = "200", description = "Bail résilié")
    @ApiResponse(responseCode = "400", description = "Date d'effet invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "409", description = "Bail déjà résilié, terminé ou non commencé")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/resiliation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BailResponse resilier(@PathVariable @Positive Integer id,
                                 @Valid @RequestBody ResiliationBailRequest request) {
        return bailService.resilier(id, request);
    }

    // ── renouveler() — POST /gestion-locative/bail/renouvellement/{id} ───────

    /**
     * Renouvelle un bail. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Renouveler un bail arrivant à échéance")
    @ApiResponse(responseCode = "200", description = "Bail renouvelé")
    @ApiResponse(responseCode = "400", description = "Nouvelle échéance invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "409", description = "Bail résilié ou période ajoutée occupée")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/renouvellement/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BailResponse renouveler(@PathVariable @Positive Integer id,
                                   @Valid @RequestBody RenouvellementBailRequest request) {
        return bailService.renouveler(id, request);
    }

    // ── reviser() — POST /gestion-locative/bail/revision/{id} ────────────────

    /**
     * Révise le loyer d'un bail. Réponses : 200 | 400 | 404 | 409 | 500.
     */

    @Operation(summary = "Réviser le loyer en conservant l'historique")
    @ApiResponse(responseCode = "200", description = "Loyer révisé")
    @ApiResponse(responseCode = "400", description = "Montant ou date d'effet invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "409", description = "Révision déjà présente à cette date")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @PostMapping(path = "/revision/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BailResponse reviser(@PathVariable @Positive Integer id,
                                @Valid @RequestBody RevisionLoyerRequest request) {
        return bailService.reviserLoyer(id, request);
    }

    // ── delete() — DELETE /gestion-locative/bail/delete/{id} ─────────────────

    /**
     * Supprime un bail qui n'a pas commencé. Réponses : 204 | 400 | 404 | 409 |
     * 500.
     */

    @Operation(summary = "Supprimer un bail")
    @ApiResponse(responseCode = "204", description = "Bail supprimé")
    @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    @ApiResponse(responseCode = "404", description = "Bail introuvable")
    @ApiResponse(responseCode = "409", description = "Bail déjà commencé : le résilier")
    @ApiResponse(responseCode = "500", description = "Erreur interne générique")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        bailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

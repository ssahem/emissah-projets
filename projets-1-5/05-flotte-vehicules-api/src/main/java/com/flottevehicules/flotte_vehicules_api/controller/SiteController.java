package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.KilometrageSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.SiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeSiteResponse;
import com.flottevehicules.flotte_vehicules_api.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
 * Endpoints des sites, du parc d'un site (F6) et du kilométrage mensuel (F19).
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/site/... — ressource au singulier, action dans le chemin.
 * RÔLE         : Endpoints des sites, du parc d'un site (F6) et du kilométrage mensuel (F19).
 * SÉCURITÉ     : aucune ; l'énoncé ne prévoit aucun utilisateur.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/site")
@Tag(name = "Site", description = "CRUD des sites, véhicules d'un site (F6) et kilométrage (F19)")
public class SiteController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final SiteService service;

    public SiteController(SiteService service) {
        this.service = service;
    }


    // ── create() — POST /flotte-vehicules/site/create ────────────────────────

    /**
     * Créer un site.
     */

    @PostMapping("/create")
    @Operation(summary = "Créer un site", description = "Nom, ville et adresse obligatoires ; nom unique")
    @ApiResponse(responseCode = "201", description = "Site créé")
    @ApiResponse(responseCode = "400", description = "Corps invalide")
    @ApiResponse(responseCode = "409", description = "Nom déjà utilisé")
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody CreateSiteRequest request) {
        SiteResponse site = service.create(request);
        return ResponseEntity.created(URI.create("/flotte-vehicules/site/" + site.idSite())).body(site);
    }


    // ── list() — GET /flotte-vehicules/site/list ─────────────────────────────

    /**
     * Lister les sites.
     */

    @GetMapping("/list")
    @Operation(summary = "Lister les sites", description = "Tri par ville puis par nom")
    @ApiResponse(responseCode = "200", description = "Liste des sites")
    public List<SiteResponse> list() {
        return service.findAll();
    }


    // ── get() — GET /flotte-vehicules/site/{id} ──────────────────────────────

    /**
     * Consulter un site.
     */

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un site")
    @ApiResponse(responseCode = "200", description = "Site trouvé")
    @ApiResponse(responseCode = "404", description = "Site inconnu")
    public SiteResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }


    // ── update() — PUT /flotte-vehicules/site/{id}/update ────────────────────

    /**
     * Modifier un site.
     */

    @PutMapping("/{id}/update")
    @Operation(summary = "Modifier un site", description = "Seuls l'adresse et la ville sont modifiables")
    @ApiResponse(responseCode = "200", description = "Site modifié")
    @ApiResponse(responseCode = "404", description = "Site inconnu")
    public SiteResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateSiteRequest request) {
        return service.update(id, request);
    }


    // ── delete() — DELETE /flotte-vehicules/site/{id}/delete ─────────────────

    /**
     * Supprimer un site.
     */

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer un site", description = "Refusé si le site possède des véhicules")
    @ApiResponse(responseCode = "204", description = "Site supprimé")
    @ApiResponse(responseCode = "409", description = "Site encore utilisé")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    // ── vehicules() — GET /flotte-vehicules/site/{id}/vehicules ──────────────

    /**
     * Véhicules d'un site.
     */

    @GetMapping("/{id}/vehicules")
    @Operation(summary = "Véhicules d'un site", description = "F6 — tri par immatriculation et état d'affectation")
    @ApiResponse(responseCode = "200", description = "Parc du site")
    @ApiResponse(responseCode = "404", description = "Site inconnu")
    public List<VehiculeSiteResponse> vehicules(@PathVariable Integer id) {
        return service.vehicules(id);
    }


    // ── kilometrage() — GET /flotte-vehicules/site/{id}/kilometrage ──────────

    /**
     * Kilométrage mensuel d'un site.
     */

    @GetMapping("/{id}/kilometrage")
    @Operation(summary = "Kilométrage mensuel d'un site", description = "F19 — mois au format yyyy-MM")
    @ApiResponse(responseCode = "200", description = "Total et détail par véhicule")
    @ApiResponse(responseCode = "400", description = "Mois invalide")
    @ApiResponse(responseCode = "404", description = "Site inconnu")
    public KilometrageSiteResponse kilometrage(@PathVariable Integer id,
                                               @RequestParam(required = false) String mois) {
        return service.kilometrage(id, mois);
    }
}

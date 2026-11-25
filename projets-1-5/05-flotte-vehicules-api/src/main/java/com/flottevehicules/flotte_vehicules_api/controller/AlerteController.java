package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.response.AlerteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.AlertesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.service.AlerteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation et déclenchement manuel des alertes (F12 et F16).
 *
 * COUCHE       : Controller (interface HTTP)
 * RÔLE         : permet de vérifier la tâche planifiée sans attendre la nuit.
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de calcul d'échéance.
 */
@RestController
@RequestMapping("/flotte-vehicules/alerte")
@Tag(name = "Alerte", description = "Alertes d'échéance (F16) et de conflit (F12)")
public class AlerteController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final AlerteService service;

    public AlerteController(AlerteService service) {
        this.service = service;
    }


    // ── list() — GET /flotte-vehicules/alerte/list ───────────────────────────

    /**
     * Lister les alertes.
     */

    @GetMapping("/list")
    @Operation(summary = "Lister les alertes", description = "Consultation des alertes F12 et F16")
    @ApiResponse(responseCode = "200", description = "Liste des alertes")
    public List<AlerteResponse> list(@RequestParam(required = false) Integer idVehicule) {
        return service.lister(idVehicule);
    }


    // ── get() — GET /flotte-vehicules/alerte/{id} ────────────────────────────

    /**
     * Consulter une alerte.
     */

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une alerte")
    @ApiResponse(responseCode = "200", description = "Alerte trouvée")
    @ApiResponse(responseCode = "404", description = "Alerte inconnue")
    public AlerteResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }


    // ── generer() — POST /flotte-vehicules/alerte/generer ────────────────────

    /**
     * Générer les alertes dues.
     */

    @PostMapping("/generer")
    @Operation(summary = "Générer les alertes dues", description = "F16 — déclenchement manuel de la tâche nocturne")
    @ApiResponse(responseCode = "200", description = "Alertes produites par site")
    public List<AlertesParSiteResponse> generer() {
        return service.genererAlertes();
    }
}

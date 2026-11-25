package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.response.HistoriqueEntretienResponse;
import com.flottevehicules.flotte_vehicules_api.service.EntretienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Historique d'entretien d'un véhicule (F8).
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/vehicule/{id}/entretiens, pour coller à l'énoncé.
 * RÔLE         : Historique d'entretien d'un véhicule (F8).
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/vehicule")
@Tag(name = "Historique entretien", description = "Historique d'entretien d'un véhicule (F8)")
public class HistoriqueEntretienController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final EntretienService service;

    public HistoriqueEntretienController(EntretienService service) {
        this.service = service;
    }


    // ── historique() — GET /flotte-vehicules/vehicule/{id}/entretiens ────────

    /**
     * Historique d'entretien d'un véhicule.
     */

    @GetMapping("/{id}/entretiens")
    @Operation(summary = "Historique d'entretien d'un véhicule", description = "F8 — tri décroissant et cumul")
    @ApiResponse(responseCode = "200", description = "Historique et cumul")
    @ApiResponse(responseCode = "404", description = "Véhicule inconnu")
    public HistoriqueEntretienResponse historique(@PathVariable Integer id) {
        return service.historique(id);
    }
}

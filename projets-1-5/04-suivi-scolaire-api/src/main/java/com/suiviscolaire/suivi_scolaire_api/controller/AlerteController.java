package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.response.AlerteDifficulteResponse;
import com.suiviscolaire.suivi_scolaire_api.service.AlertesDifficulteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation des alertes de difficulté produites par la tâche F16.
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : exposer en lecture les alertes déjà persistées, sans redéclencher la
 *                détection.
 * URL DE BASE  : /suivi-scolaire/alerte
 * ENDPOINTS    : list?trimestre=…
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : AlertesDifficulteService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique de détection, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/alerte")
@Tag(name="Alertes", description="Consultation des alertes de difficulté (F16)")
public class AlerteController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final AlertesDifficulteService service;

    /** Spring injecte le service par constructeur. */
    public AlerteController(AlertesDifficulteService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── list() — GET /suivi-scolaire/alerte/list ─────────────────────────────

    /**
     * Alertes d'un trimestre.
     */

    @Operation(summary = "Alertes d'un trimestre",
            description = "Endpoint facultatif de consultation : la détection reste une tâche planifiée (F16)")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AlerteDifficulteResponse> list(@RequestParam Integer trimestre) {
        return service.consulter(trimestre);
    }
}

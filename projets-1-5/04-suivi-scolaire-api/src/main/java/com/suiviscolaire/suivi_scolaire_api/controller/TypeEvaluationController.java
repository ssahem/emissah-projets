package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdatePoidsRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PoidsResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ReglesMetierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de configuration du poids des types d'évaluation (F13).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir le code du type (DEV, INTERRO, ORAL, libellé ou identifiant) et le
 *                nouveau poids.
 * URL DE BASE  : /suivi-scolaire/type-evaluation
 * ENDPOINTS    : poids/{code}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : ReglesMetierService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/type-evaluation")
@Tag(name="Types d'évaluation", description="Poids des devoirs, interrogations et oraux (F13)")
public class TypeEvaluationController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final ReglesMetierService service;

    /** Spring injecte le service par constructeur. */
    public TypeEvaluationController(ReglesMetierService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── modifierPoids() — PUT /suivi-scolaire/type-evaluation/poids/{code} ───

    /**
     * Définir le poids d'un type.
     */

    @Operation(summary = "Définir le poids d'un type",
            description = "Code DEV, INTERRO ou ORAL (ou libellé/identifiant) ; refusé si un trimestre est clôturé (F13)")
    @PutMapping(path = "/poids/{code}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PoidsResponse modifierPoids(@PathVariable String code,
                                       @Valid @RequestBody UpdatePoidsRequest request) {
        return service.modifierPoids(code, request.poids());
    }
}

package com.suiviscolaire.suivi_scolaire_api.controller;

import com.suiviscolaire.suivi_scolaire_api.dto.request.ReouvertureRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClotureResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ReouvertureResponse;
import com.suiviscolaire.suivi_scolaire_api.service.ReglesMetierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de clôture et de réouverture des trimestres (F14, F15).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : figer les notes d'un trimestre complet ou rouvrir le dernier trimestre
 *                clôturé.
 * URL DE BASE  : /suivi-scolaire/trimestre
 * ENDPOINTS    : {numero}/cloture · {numero}/reouverture
 * SÉCURITÉ     : aucune authentification — API publique (énoncé), auteur fourni dans le corps.
 * DÉPEND DE    : ReglesMetierService.
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/suivi-scolaire/trimestre")
@Tag(name="Trimestres", description="Clôture et réouverture des trimestres (F14, F15)")
public class TrimestreController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final ReglesMetierService service;

    /** Spring injecte le service par constructeur. */
    public TrimestreController(ReglesMetierService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    // ── cloturer() — POST /suivi-scolaire/trimestre/{numero}/cloture ─────────

    /**
     * Clôturer un trimestre.
     */

    @Operation(summary = "Clôturer un trimestre",
            description = "Exige au moins une note pour chaque enseignement ; sinon 409 avec la liste (F14)")
    @PostMapping(path = "/{numero}/cloture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClotureResponse cloturer(@PathVariable Integer numero) {
        return service.cloturer(numero);
    }

    // ── reouvrir() — POST /suivi-scolaire/trimestre/{numero}/reouverture ─────

    /**
     * Rouvrir le dernier trimestre clôturé.
     */

    @Operation(summary = "Rouvrir le dernier trimestre clôturé",
            description = "Motif d'au moins 20 caractères et auteur texte fourni par l'appelant (F15, A-04)")
    @PostMapping(path = "/{numero}/reouverture", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ReouvertureResponse reouvrir(@PathVariable Integer numero,
                                        @Valid @RequestBody ReouvertureRequest request) {
        return service.reouvrir(numero, request.motif(), request.auteur());
    }
}

package com.interventionsterrain.interventions_terrain_api.controller;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.ClientResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.HistoriqueClientResponse;
import com.interventionsterrain.interventions_terrain_api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints CRUD des clients (F4).
 *
 * COUCHE       : Contrôleur (point d'entrée HTTP)
 * RÔLE         : recevoir les requêtes HTTP de Client, valider les paramètres et déléguer au
 *                service.
 * URL DE BASE  : /interventions-terrain/client
 * ENDPOINTS    : list · get/{id} · interventions/{id} · create · update/{id} · delete/{id}
 * SÉCURITÉ     : aucune authentification — API publique (énoncé).
 * DÉPEND DE    : ClientService
 * UTILISÉ PAR  : les clients HTTP de l'API.
 * NE FAIT PAS  : de logique métier, d'accès base ni de try/catch.
 */
@RestController
@RequestMapping("/interventions-terrain/client")
@Tag(name = "Clients", description = "Gestion des clients (F4)")
public class ClientController {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS ET CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Lister les clients", description = "Triés par nom")
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ClientResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Lire un client")
    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClientResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @Operation(summary = "Consulter l'historique d'un client",
            description = "Interventions du plus récent au plus ancien (F8)")
    @GetMapping(path = "/interventions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HistoriqueClientResponse> historique(@PathVariable Integer id) {
        return service.findHistorique(id);
    }

    @Operation(summary = "Créer un client")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/interventions-terrain/client/get/" + created.idClient()))
                .body(created);
    }

    @Operation(summary = "Modifier un client")
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ClientResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateClientRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer un client")
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

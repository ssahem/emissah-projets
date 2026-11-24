package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.CoutVehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.DisponibilitesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.ProchainEntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
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
 * Endpoints des véhicules : CRUD (F2), recherche (F9), disponibilité (F17),
 * prochain entretien (F15) et coût (F18).
 *
 * COUCHE       : Controller (interface HTTP)
 * URL          : /flotte-vehicules/vehicule/... — ressource au singulier, action dans le
 *                chemin.
 * SÉCURITÉ     : aucune.
 * NE FAIT PAS  : de logique métier.
 */
@RestController
@RequestMapping("/flotte-vehicules/vehicule")
@Tag(name = "Véhicule", description = "CRUD, recherche, disponibilité, échéance et coût")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    /** F2 : crée un véhicule. */
    @PostMapping("/create")
    @Operation(summary = "Créer un véhicule", description = "Immatriculation unique et date de mise en service non future")
    @ApiResponse(responseCode = "201", description = "Véhicule créé")
    @ApiResponse(responseCode = "400", description = "Donnée invalide")
    @ApiResponse(responseCode = "409", description = "Immatriculation déjà utilisée")
    public ResponseEntity<VehiculeResponse> create(@Valid @RequestBody CreateVehiculeRequest request) {
        VehiculeResponse vehicule = service.create(request);
        return ResponseEntity.created(URI.create("/flotte-vehicules/vehicule/" + vehicule.idVehicule()))
                .body(vehicule);
    }

    /** F2 et F9 : liste ou recherche combinée. */
    @GetMapping("/list")
    @Operation(summary = "Lister ou rechercher des véhicules",
            description = "F2 et F9 — critères optionnels : immatriculation partielle et marque exacte")
    @ApiResponse(responseCode = "200", description = "Véhicules trouvés")
    public List<VehiculeResponse> list(@RequestParam(required = false) String immatriculation,
                                       @RequestParam(required = false) String marque) {
        return service.findAll(immatriculation, marque);
    }

    /** F2 : détail d'un véhicule. */
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un véhicule")
    @ApiResponse(responseCode = "200", description = "Véhicule trouvé")
    @ApiResponse(responseCode = "404", description = "Véhicule inconnu")
    public VehiculeResponse get(@PathVariable Integer id) {
        return service.findById(id);
    }

    /** F2 : transfert de site, marque et modèle modifiables. */
    @PutMapping("/{id}/update")
    @Operation(summary = "Modifier un véhicule", description = "Transfert de site possible ; immatriculation et date figées")
    @ApiResponse(responseCode = "200", description = "Véhicule modifié")
    @ApiResponse(responseCode = "404", description = "Véhicule inconnu")
    public VehiculeResponse update(@PathVariable Integer id,
                                   @Valid @RequestBody UpdateVehiculeRequest request) {
        return service.update(id, request);
    }

    /** F2 : suppression refusée en cas d'affectations ou d'entretiens. */
    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Supprimer un véhicule")
    @ApiResponse(responseCode = "204", description = "Véhicule supprimé")
    @ApiResponse(responseCode = "409", description = "Véhicule encore utilisé")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** F17 : véhicules disponibles sur une période, regroupés par site. */
    @GetMapping("/disponibles")
    @Operation(summary = "Véhicules disponibles", description = "F17 — sans paramètre, semaine en cours")
    @ApiResponse(responseCode = "200", description = "Disponibilités par site")
    @ApiResponse(responseCode = "400", description = "Période invalide")
    public List<DisponibilitesParSiteResponse> disponibles(
            @RequestParam(required = false) LocalDate du,
            @RequestParam(required = false) LocalDate au) {
        return service.disponibles(du, au);
    }

    /** F15 : prochaine échéance périodique. */
    @GetMapping("/{id}/prochain-entretien")
    @Operation(summary = "Prochain entretien", description = "F15 — premier seuil atteint parmi km et durée")
    @ApiResponse(responseCode = "200", description = "Échéance calculée")
    @ApiResponse(responseCode = "404", description = "Véhicule ou échéance inconnue")
    public ProchainEntretienResponse prochainEntretien(@PathVariable Integer id) {
        return service.prochainEntretien(id);
    }

    /** F18 : coût total des entretiens et répartition par type. */
    @GetMapping("/{id}/cout")
    @Operation(summary = "Coût total d'un véhicule", description = "F18 — entretiens uniquement (D-03)")
    @ApiResponse(responseCode = "200", description = "Coût et répartition")
    @ApiResponse(responseCode = "404", description = "Véhicule inconnu")
    public CoutVehiculeResponse cout(@PathVariable Integer id) {
        return service.cout(id);
    }
}

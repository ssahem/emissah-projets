package com.flottevehicules.flotte_vehicules_api.controller;

import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation en lecture seule des dix vues du schéma.
 *
 * COUCHE       : Controller (interface HTTP)
 * RÔLE         : outil de diagnostic ; les vues ne sont jamais mappées comme entités JPA.
 * SÉCURITÉ     : aucune ; la liste des noms est fermée dans VueRepository (aucune injection
 *                SQL).
 * NE FAIT PAS  : d'écriture.
 */
@RestController
@RequestMapping("/flotte-vehicules/vue")
@Tag(name = "Vue SQL", description = "Lecture seule des dix vues du schéma")
public class VueController {

    private final VueRepository repository;

    public VueController(VueRepository repository) {
        this.repository = repository;
    }

    /** Retourne le contenu d'une vue autorisée. */
    @GetMapping("/{nom}")
    @Operation(summary = "Lire une vue SQL", description = "Noms acceptés : les dix vues du schéma")
    @ApiResponse(responseCode = "200", description = "Lignes de la vue")
    @ApiResponse(responseCode = "400", description = "Nom de vue non autorisé")
    public List<Map<String, Object>> lire(@PathVariable String nom) {
        return repository.lireVue(nom);
    }
}

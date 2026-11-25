package com.billetterie.billetterie_api.service;

import com.billetterie.billetterie_api.dto.request.CreateSalleRequest;
import com.billetterie.billetterie_api.dto.response.PlaceSalleResponse;
import com.billetterie.billetterie_api.dto.response.ProgrammationResponse;
import com.billetterie.billetterie_api.dto.response.SalleResponse;
import com.billetterie.billetterie_api.dto.request.UpdateSalleRequest;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.exception.CapaciteInsuffisanteException;
import com.billetterie.billetterie_api.exception.CategorieInconnueException;
import com.billetterie.billetterie_api.exception.SalleDejaExistanteException;
import com.billetterie.billetterie_api.exception.SalleNonSupprimableException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.mapper.EvenementMapper;
import com.billetterie.billetterie_api.mapper.PlaceMapper;
import com.billetterie.billetterie_api.mapper.SalleMapper;
import com.billetterie.billetterie_api.repository.CategorieRepository;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des salles et des consultations qui leur sont rattachées.
 *
 * COUCHE       : Service
 * RÔLE         : gérer F1 et produire les restitutions F5, F6 et F7.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Salle/Place/Evenement/Categorie et mappers.
 * UTILISÉ PAR  : SalleController.
 * NE FAIT PAS  : de HTTP ni de SQL.
 */
@Service
@Transactional(readOnly = true)
public class SalleService {

    private final SalleRepository salleRepository;
    private final PlaceRepository placeRepository;
    private final EvenementRepository evenementRepository;
    private final CategorieRepository categorieRepository;
    private final SalleMapper salleMapper;
    private final PlaceMapper placeMapper;
    private final EvenementMapper evenementMapper;

    public SalleService(SalleRepository salleRepository, PlaceRepository placeRepository,
                        EvenementRepository evenementRepository, CategorieRepository categorieRepository,
                        SalleMapper salleMapper, PlaceMapper placeMapper,
                        EvenementMapper evenementMapper) {
        this.salleRepository = salleRepository;
        this.placeRepository = placeRepository;
        this.evenementRepository = evenementRepository;
        this.categorieRepository = categorieRepository;
        this.salleMapper = salleMapper;
        this.placeMapper = placeMapper;
        this.evenementMapper = evenementMapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F1
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /** Retourne toutes les salles triées par nom puis ville. */
    public List<SalleResponse> findAll() {
        return salleRepository.findAllByOrderByNomAscVilleAsc().stream()
                .map(salleMapper::toResponse)
                .toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Retourne une salle par identifiant.
     *
     * @throws SalleNotFoundException si aucune salle ne porte cet identifiant.
     */
    public SalleResponse findById(Integer idSalle) {
        return salleMapper.toResponse(requireSalle(idSalle));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée une salle ; le couple (nom, ville) doit être unique. Modifie la base.
     *
     * @throws SalleDejaExistanteException si le couple nom/ville existe déjà.
     */
    @Transactional
    public SalleResponse create(CreateSalleRequest request) {
        // ── Étape 1/3 : vérifier la clé métier ───────────────────────────────
        if (salleRepository.existsByNomIgnoreCaseAndVilleIgnoreCase(request.nom(), request.ville())) {
            throw new SalleDejaExistanteException(request.nom());
        }

        // ── Étape 2/3 : construire et enregistrer ────────────────────────────
        Salle salle = new Salle(request.nom().trim(), request.adresse().trim(),
                request.ville().trim(), request.capacite());
        Salle enregistree = salleRepository.saveAndFlush(salle);

        // ── Étape 3/3 : convertir en DTO ─────────────────────────────────────
        return salleMapper.toResponse(enregistree);
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Modifie une salle en vérifiant l'unicité et la capacité minimale. Modifie la base.
     *
     * @throws SalleNotFoundException si aucune salle ne porte cet identifiant.
     *
     * @throws CapaciteInsuffisanteException si la nouvelle capacité est inférieure aux places existantes.
     */
    @Transactional
    public SalleResponse update(Integer idSalle, UpdateSalleRequest request) {
        // ── Étape 1/3 : charger et contrôler les contraintes ─────────────────
        Salle salle = requireSalle(idSalle);
        if (salleRepository.existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdSalleNot(
                request.nom(), request.ville(), idSalle)) {
            throw new SalleDejaExistanteException(request.nom());
        }
        long places = placeRepository.countBySalle_IdSalle(idSalle);
        if (request.capacite() < places) {
            throw new CapaciteInsuffisanteException(idSalle);
        }

        // ── Étape 2/3 : appliquer les changements ────────────────────────────
        salle.modifier(request.nom().trim(), request.adresse().trim(),
                request.ville().trim(), request.capacite());

        // ── Étape 3/3 : convertir en DTO ─────────────────────────────────────
        return salleMapper.toResponse(salle);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Supprime une salle vide. Modifie la base.
     *
     * @throws SalleNotFoundException si aucune salle ne porte cet identifiant.
     *
     * @throws SalleNonSupprimableException si la salle comporte encore des places ou des événements.
     */
    @Transactional
    public void delete(Integer idSalle) {
        Salle salle = requireSalle(idSalle);
        if (salleRepository.countPlacesByIdSalle(idSalle) > 0
                || salleRepository.countEvenementsByIdSalle(idSalle) > 0) {
            throw new SalleNonSupprimableException(idSalle);
        }
        salleRepository.delete(salle);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F5, F6, F7
    // ═════════════════════════════════════════════════════════════════════════

    // ── findPlaces() ─────────────────────────────────────────────────────────

    /**
     * Retourne les places d'une salle, filtrées ou non par catégorie.
     *
     * @throws SalleNotFoundException si aucune salle ne porte cet identifiant.
     *
     * @throws CategorieInconnueException si la catégorie demandée n’existe pas.
     */
    public List<PlaceSalleResponse> findPlaces(Integer idSalle, String categorie) {
        requireSalle(idSalle);
        if (categorie == null || categorie.isBlank()) {
            return placeRepository.findAllBySalle_IdSalleOrderByRangAscNumeroAsc(idSalle).stream()
                    .map(placeMapper::toResponse)
                    .toList();
        }
        Categorie reference = categorieRepository.findByLibelleIgnoreCase(categorie)
                .orElseThrow(() -> new CategorieInconnueException(categorie));
        return placeRepository
                .findAllBySalle_IdSalleAndCategorie_LibelleIgnoreCaseOrderByRangAscNumeroAsc(
                        idSalle, reference.getLibelle()).stream()
                .map(placeMapper::toResponse)
                .toList();
    }

    // ── findProgrammation() ──────────────────────────────────────────────────

    /**
     * Retourne les événements d'une salle triés par date croissante (F7).
     *
     * @throws SalleNotFoundException si aucune salle ne porte cet identifiant.
     */
    public List<ProgrammationResponse> findProgrammation(Integer idSalle) {
        requireSalle(idSalle);
        return evenementRepository.findAllBySalle_IdSalleOrderByDateHeureAsc(idSalle).stream()
                .map(evenementMapper::toProgrammation)
                .toList();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireSalle() ───────────────────────────────────────────────────────

    /** Charge une salle ou lève l'erreur métier 404. */
    private Salle requireSalle(Integer idSalle) {
        return salleRepository.findById(idSalle)
                .orElseThrow(() -> new SalleNotFoundException(idSalle));
    }
}

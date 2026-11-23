package com.billetterie.billetterie_api.service;

import com.billetterie.billetterie_api.dto.request.CreatePlaceRequest;
import com.billetterie.billetterie_api.dto.request.UpdatePlaceRequest;
import com.billetterie.billetterie_api.dto.response.PlaceResponse;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.exception.CapaciteInsuffisanteException;
import com.billetterie.billetterie_api.exception.CategorieInconnueException;
import com.billetterie.billetterie_api.exception.PlaceDejaExistanteException;
import com.billetterie.billetterie_api.exception.PlaceNonSupprimableException;
import com.billetterie.billetterie_api.exception.PlaceNotFoundException;
import com.billetterie.billetterie_api.exception.PlacePositionFigeeException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.mapper.PlaceMapper;
import com.billetterie.billetterie_api.repository.CategorieRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier du CRUD des places physiques (F2).
 *
 * COUCHE       : Service
 * RÔLE         : créer, lire, modifier et supprimer les places d'une salle.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Place/Salle/Catégorie/RéservationPlace, mapper.
 * NE FAIT PAS  : le HTTP ni la validation de format des champs.
 */
@Service
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final SalleRepository salleRepository;
    private final CategorieRepository categorieRepository;
    private final ReservationPlaceRepository reservationPlaceRepository;
    private final PlaceMapper placeMapper;

    public PlaceService(PlaceRepository placeRepository, SalleRepository salleRepository,
                        CategorieRepository categorieRepository,
                        ReservationPlaceRepository reservationPlaceRepository, PlaceMapper placeMapper) {
        this.placeRepository = placeRepository;
        this.salleRepository = salleRepository;
        this.categorieRepository = categorieRepository;
        this.reservationPlaceRepository = reservationPlaceRepository;
        this.placeMapper = placeMapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F2
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /** Retourne toutes les places triées par rang puis par numéro. */
    public List<PlaceResponse> findAll() {
        return placeRepository.findAllByOrderByRangAscNumeroAsc().stream()
                .map(placeMapper::toPlaceResponse)
                .toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /** Retourne une place par identifiant. */
    public PlaceResponse findById(Integer idPlace) {
        return placeMapper.toPlaceResponse(requirePlace(idPlace));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /** Crée une place dans le respect de la capacité et de l'unicité. Modifie la base. */
    @Transactional
    public PlaceResponse create(CreatePlaceRequest request) {
        // ── Étape 1/3 : résoudre la salle et la catégorie ──
        Salle salle = requireSalle(request.idSalle());
        Categorie categorie = requireCategorie(request.categorie());

        // ── Étape 2/3 : appliquer les règles métier ──
        requireCapacite(salle);
        if (placeRepository.existsBySalle_IdSalleAndRangAndNumero(
                request.idSalle(), request.rang(), request.numero())) {
            throw new PlaceDejaExistanteException(request.rang());
        }

        // ── Étape 3/3 : enregistrer et convertir ──
        Place place = placeRepository.saveAndFlush(
                new Place(request.rang().trim(), request.numero(), salle, categorie));
        return placeMapper.toPlaceResponse(place);
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /** Modifie une place ; sa position est figée dès la première réservation. Modifie la base. */
    @Transactional
    public PlaceResponse update(Integer idPlace, UpdatePlaceRequest request) {
        // ── Étape 1/3 : charger et résoudre ──
        Place place = requirePlace(idPlace);
        Salle salle = requireSalle(request.idSalle());
        Categorie categorie = requireCategorie(request.categorie());

        // ── Étape 2/3 : appliquer les règles de figeté, d'unicité et de capacité ──
        boolean positionModifiee = !Objects.equals(place.getRang(), request.rang())
                || !Objects.equals(place.getNumero(), request.numero())
                || !Objects.equals(place.getSalle().getIdSalle(), request.idSalle());
        if (positionModifiee
                && reservationPlaceRepository.existsByRangAndNumero(place.getRang(), place.getNumero())) {
            throw new PlacePositionFigeeException(idPlace);
        }
        if (positionModifiee && placeRepository.existsBySalle_IdSalleAndRangAndNumero(
                request.idSalle(), request.rang(), request.numero())) {
            throw new PlaceDejaExistanteException(request.rang());
        }
        if (positionModifiee && !Objects.equals(place.getSalle().getIdSalle(), request.idSalle())) {
            requireCapacite(salle);
        }

        // ── Étape 3/3 : appliquer et convertir ──
        place.modifier(request.rang().trim(), request.numero(), salle, categorie);
        return placeMapper.toPlaceResponse(place);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /** Supprime une place non réservée. Modifie la base. */
    @Transactional
    public void delete(Integer idPlace) {
        Place place = requirePlace(idPlace);
        if (reservationPlaceRepository.existsByRangAndNumero(place.getRang(), place.getNumero())) {
            throw new PlaceNonSupprimableException(idPlace);
        }
        placeRepository.delete(place);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requirePlace() ───────────────────────────────────────────────────────

    /** Charge une place ou lève l'erreur 404. */
    private Place requirePlace(Integer idPlace) {
        return placeRepository.findById(idPlace)
                .orElseThrow(() -> new PlaceNotFoundException(idPlace.toString()));
    }

    // ── requireSalle() ───────────────────────────────────────────────────────

    /** Charge une salle ou lève l'erreur 404. */
    private Salle requireSalle(Integer idSalle) {
        return salleRepository.findById(idSalle)
                .orElseThrow(() -> new SalleNotFoundException(idSalle));
    }

    // ── requireCategorie() ───────────────────────────────────────────────────

    /** Résout une catégorie ou lève l'erreur 400. */
    private Categorie requireCategorie(String libelle) {
        return categorieRepository.findByLibelleIgnoreCase(libelle)
                .orElseThrow(() -> new CategorieInconnueException(libelle));
    }

    // ── requireCapacite() ────────────────────────────────────────────────────

    /** Refuse de dépasser la capacité de la salle. */
    private void requireCapacite(Salle salle) {
        if (placeRepository.countBySalle_IdSalle(salle.getIdSalle()) >= salle.getCapacite()) {
            throw new CapaciteInsuffisanteException(salle.getIdSalle());
        }
    }
}

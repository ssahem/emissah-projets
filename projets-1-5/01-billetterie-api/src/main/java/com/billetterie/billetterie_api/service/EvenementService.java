package com.billetterie.billetterie_api.service;

import com.billetterie.billetterie_api.dto.request.CreateEvenementRequest;
import com.billetterie.billetterie_api.dto.response.EvenementResponse;
import com.billetterie.billetterie_api.dto.response.PlacesLibresParCategorieResponse;
import com.billetterie.billetterie_api.dto.response.PlacesLibresResponse;
import com.billetterie.billetterie_api.dto.response.RemplissageParCategorieResponse;
import com.billetterie.billetterie_api.dto.response.RemplissageResponse;
import com.billetterie.billetterie_api.dto.request.UpdateEvenementRequest;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.exception.DateEvenementPasseeException;
import com.billetterie.billetterie_api.exception.EvenementNonSupprimableException;
import com.billetterie.billetterie_api.exception.EvenementNotFoundException;
import com.billetterie.billetterie_api.exception.PeriodeInvalideException;
import com.billetterie.billetterie_api.exception.SalleEvenementFigeeException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.exception.SalleSansPlaceException;
import com.billetterie.billetterie_api.mapper.CsvExportMapper;
import com.billetterie.billetterie_api.mapper.EvenementMapper;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import com.billetterie.billetterie_api.repository.SpectacleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des événements, de la recherche F8 et des restitutions F17, F18 et F19.
 *
 * COUCHE       : Service
 * RÔLE         : gérer F3, F8 et produire les restitutions F17, F18 et F19.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Evenement/Salle/Spectacle/Place/Réservation, Clock, mappers.
 * UTILISÉ PAR  : EvenementController.
 * NE FAIT PAS  : le HTTP ni la validation de format des dates.
 */
@Service
@Transactional(readOnly = true)
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final SalleRepository salleRepository;
    private final SpectacleRepository spectacleRepository;
    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationPlaceRepository reservationPlaceRepository;
    private final EvenementMapper evenementMapper;
    private final CsvExportMapper csvExportMapper;
    private final Clock clock;

    public EvenementService(EvenementRepository evenementRepository, SalleRepository salleRepository,
                            SpectacleRepository spectacleRepository, PlaceRepository placeRepository,
                            ReservationRepository reservationRepository,
                            ReservationPlaceRepository reservationPlaceRepository,
                            EvenementMapper evenementMapper, CsvExportMapper csvExportMapper,
                            Clock clock) {
        this.evenementRepository = evenementRepository;
        this.salleRepository = salleRepository;
        this.spectacleRepository = spectacleRepository;
        this.placeRepository = placeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationPlaceRepository = reservationPlaceRepository;
        this.evenementMapper = evenementMapper;
        this.csvExportMapper = csvExportMapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F3
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /** Retourne tous les événements triés par date croissante. */
    public List<EvenementResponse> findAll() {
        return evenementRepository.findAllByOrderByDateHeureAsc().stream()
                .map(evenementMapper::toResponse)
                .toList();
    }

    // ── findBetween() ────────────────────────────────────────────────────────

    /**
     * Filtre les événements entre deux bornes inclusives, une borne ou aucune (F8).
     *
     * @throws PeriodeInvalideException si la borne de début est postérieure à la borne de fin.
     */
    public List<EvenementResponse> findBetween(LocalDateTime du, LocalDateTime au) {
        // ── Étape 1/2 : valider la période ──
        if (du != null && au != null && du.isAfter(au)) {
            throw new PeriodeInvalideException(du.toString());
        }

        // ── Étape 2/2 : choisir la requête et convertir ──
        List<Evenement> evenements;
        if (du == null && au == null) {
            evenements = evenementRepository.findAllByOrderByDateHeureAsc();
        } else if (du == null) {
            evenements = evenementRepository.findAllByDateHeureLessThanEqualOrderByDateHeureAsc(au);
        } else if (au == null) {
            evenements = evenementRepository.findAllByDateHeureGreaterThanEqualOrderByDateHeureAsc(du);
        } else {
            evenements = evenementRepository.findAllByDateHeureBetweenOrderByDateHeureAsc(du, au);
        }
        return evenements.stream().map(evenementMapper::toResponse).toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Retourne un événement par identifiant.
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     */
    public EvenementResponse findById(Integer idEvenement) {
        return evenementMapper.toResponse(requireEvenement(idEvenement));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée un événement futur dans une salle pourvue de places. Modifie la base.
     *
     * @throws DateEvenementPasseeException si la date de l’événement est déjà passée.
     *
     * @throws SalleSansPlaceException si la salle ne contient aucune place.
     */
    @Transactional
    public EvenementResponse create(CreateEvenementRequest request) {
        // ── Étape 1/3 : résoudre les références et contrôler les règles ──
        Salle salle = requireSalle(request.idSalle());
        requireFutur(request.dateHeure());
        requireSalleAvecPlace(salle);

        // ── Étape 2/3 : réutiliser ou créer le spectacle, puis enregistrer ──
        Spectacle spectacle = spectacleRepository
                .findFirstByTitreIgnoreCaseAndType(request.titre(), request.type())
                .orElseGet(() -> spectacleRepository.saveAndFlush(new Spectacle(
                        request.titre().trim(), request.type(), request.placementNumerote())));
        Evenement evenement = evenementRepository.saveAndFlush(
                new Evenement(request.dateHeure(), spectacle, salle));

        // ── Étape 3/3 : convertir en DTO ──
        return evenementMapper.toResponse(evenement);
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Modifie un événement ; la salle est figée dès la première réservation. Modifie la base.
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     *
     * @throws SalleEvenementFigeeException si la salle ou la date ne peut plus être modifiée.
     */
    @Transactional
    public EvenementResponse update(Integer idEvenement, UpdateEvenementRequest request) {
        // ── Étape 1/3 : charger et contrôler ──
        Evenement evenement = requireEvenement(idEvenement);
        requireFutur(request.dateHeure());
        Salle salle = requireSalle(request.idSalle());
        requireSalleAvecPlace(salle);
        if (!Objects.equals(evenement.getSalle().getIdSalle(), request.idSalle())
                && evenementRepository.countReservationsByIdEvenement(idEvenement) > 0) {
            throw new SalleEvenementFigeeException(idEvenement);
        }

        // ── Étape 2/3 : réutiliser ou créer le spectacle, puis appliquer ──
        Spectacle spectacle = spectacleRepository
                .findFirstByTitreIgnoreCaseAndType(request.titre(), request.type())
                .orElseGet(() -> spectacleRepository.saveAndFlush(new Spectacle(
                        request.titre().trim(), request.type(), request.placementNumerote())));
        evenement.setDateHeure(request.dateHeure());
        evenement.setSalle(salle);
        evenement.setSpectacle(spectacle);

        // ── Étape 3/3 : convertir en DTO ──
        return evenementMapper.toResponse(evenement);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Supprime un événement sans réservation. Modifie la base.
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     *
     * @throws EvenementNonSupprimableException si l’événement comporte encore des réservations.
     */
    @Transactional
    public void delete(Integer idEvenement) {
        Evenement evenement = requireEvenement(idEvenement);
        if (evenementRepository.countReservationsByIdEvenement(idEvenement) > 0) {
            throw new EvenementNonSupprimableException(idEvenement);
        }
        evenementRepository.delete(evenement);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F17 et F19
    // ═════════════════════════════════════════════════════════════════════════

    // ── findPlacesLibres() ───────────────────────────────────────────────────

    /**
     * Calcule les places encore libres, groupées par catégorie (F17).
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     *
     * @throws SalleSansPlaceException si la salle ne contient aucune place.
     */
    public PlacesLibresResponse findPlacesLibres(Integer idEvenement) {
        // ── Étape 1/3 : charger l'événement et les places de sa salle ──
        Evenement evenement = requireEvenement(idEvenement);
        List<Place> places = placeRepository
                .findAllBySalle_IdSalleOrderByRangAscNumeroAsc(evenement.getSalle().getIdSalle());

        // ── Étape 2/3 : déduire les places actives ──
        Map<String, Long> occupees = new LinkedHashMap<>();
        for (ReservationPlace rp : reservationPlaceRepository.findPlacesActivesByEvenement(idEvenement)) {
            String categorie = placeRepository
                    .findBySalle_IdSalleAndRangAndNumero(evenement.getSalle().getIdSalle(),
                            rp.getRang(), rp.getNumero())
                    .map(place -> place.getCategorie().getLibelle())
                    .orElse("INCONNUE");
            occupees.merge(categorie, 1L, Long::sum);
        }

        // ── Étape 3/3 : grouper les places libres par catégorie ──
        Map<String, long[]> cumuls = new LinkedHashMap<>();
        for (Place place : places) {
            String categorie = place.getCategorie().getLibelle();
            long[] totals = cumuls.computeIfAbsent(categorie, c -> new long[2]);
            totals[0]++;
        }
        List<PlacesLibresParCategorieResponse> detail = new ArrayList<>();
        cumuls.forEach((categorie, totals) -> detail.add(new PlacesLibresParCategorieResponse(
                categorie, totals[0] - occupees.getOrDefault(categorie, 0L),
                occupees.getOrDefault(categorie, 0L))));
        return new PlacesLibresResponse(idEvenement, places.size(), detail);
    }

    // ── findRemplissage() ────────────────────────────────────────────────────

    /**
     * Calcule le taux de remplissage d'un événement, détaillé par catégorie (F18).
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     *
     * @throws SalleSansPlaceException si la salle ne contient aucune place.
     */
    public RemplissageResponse findRemplissage(Integer idEvenement) {
        // ── Étape 1/4 : charger l'événement et vérifier les places de la salle ──
        Evenement evenement = requireEvenement(idEvenement);
        Integer idSalle = evenement.getSalle().getIdSalle();
        List<Place> places = placeRepository.findAllBySalle_IdSalleOrderByRangAscNumeroAsc(idSalle);
        if (places.isEmpty()) {
            throw new SalleSansPlaceException(idSalle);
        }

        // ── Étape 2/4 : initialiser les cumuls par catégorie ──
        // Chaque tableau contient : [total, provisoires, payees].
        Map<String, long[]> cumuls = new LinkedHashMap<>();
        for (Place place : places) {
            cumuls.computeIfAbsent(place.getCategorie().getLibelle(), c -> new long[3])[0]++;
        }

        // ── Étape 3/4 : compter les réservations actives, ANNULEE exclue ──
        long provisoires = compterParStatut(idSalle, idEvenement, "PROVISOIRE", cumuls, 1);
        long payees = compterParStatut(idSalle, idEvenement, "PAYEE", cumuls, 2);

        // ── Étape 4/4 : assembler la réponse ──
        List<RemplissageParCategorieResponse> detail = new ArrayList<>();
        cumuls.forEach((categorie, t) -> detail.add(new RemplissageParCategorieResponse(
                categorie, t[0], t[1], t[2], pourcentage(t[1] + t[2], t[0]))));
        return new RemplissageResponse(idEvenement, places.size(), provisoires, payees,
                pourcentage(provisoires + payees, places.size()), detail);
    }

    // ── exportReservations() ─────────────────────────────────────────────────

    /**
     * Exporte les réservations de l'événement au format CSV (F19).
     *
     * @throws EvenementNotFoundException si aucun événement ne porte cet identifiant.
     */
    public String exportReservations(Integer idEvenement, boolean inclureAnnulees) {
        requireEvenement(idEvenement);
        return csvExportMapper.toCsv(
                reservationRepository.findLignesExport(idEvenement, inclureAnnulees));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── compterParStatut() ───────────────────────────────────────────────────

    /** Compte les places d'un statut et les ventile dans les cumuls par catégorie (F18). */
    private long compterParStatut(Integer idSalle, Integer idEvenement, String statut,
                                  Map<String, long[]> cumuls, int index) {
        long total = 0;
        for (ReservationPlace place : reservationPlaceRepository
                .findPlacesByEvenementAndStatut(idEvenement, statut)) {
            String categorie = placeRepository.findBySalle_IdSalleAndRangAndNumero(
                            idSalle, place.getRang(), place.getNumero())
                    .map(p -> p.getCategorie().getLibelle())
                    .orElse("INCONNUE");
            cumuls.computeIfAbsent(categorie, c -> new long[3])[index]++;
            total++;
        }
        return total;
    }

    // ── pourcentage() ────────────────────────────────────────────────────────

    /** Calcule un pourcentage arrondi à deux décimales. */
    private BigDecimal pourcentage(long occupees, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(occupees)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    // ── requireEvenement() ───────────────────────────────────────────────────

    /** Charge un événement ou lève l'erreur 404. */
    private Evenement requireEvenement(Integer idEvenement) {
        return evenementRepository.findById(idEvenement)
                .orElseThrow(() -> new EvenementNotFoundException(idEvenement));
    }

    // ── requireSalle() ───────────────────────────────────────────────────────

    /** Charge une salle ou lève l'erreur 404. */
    private Salle requireSalle(Integer idSalle) {
        return salleRepository.findById(idSalle)
                .orElseThrow(() -> new SalleNotFoundException(idSalle));
    }

    // ── requireFutur() ───────────────────────────────────────────────────────

    /** Vérifie que la date est dans le futur (format déjà validé par le DTO). */
    private void requireFutur(LocalDateTime dateHeure) {
        if (!dateHeure.isAfter(LocalDateTime.now(clock))) {
            throw new DateEvenementPasseeException();
        }
    }

    // ── requireSalleAvecPlace() ──────────────────────────────────────────────

    /** Refuse une salle sans place (règle F3). */
    private void requireSalleAvecPlace(Salle salle) {
        if (placeRepository.countBySalle_IdSalle(salle.getIdSalle()) == 0) {
            throw new SalleSansPlaceException(salle.getIdSalle());
        }
    }
}

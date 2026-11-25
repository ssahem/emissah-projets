package com.billetterie.billetterie_api.service;

import com.billetterie.billetterie_api.dto.request.CreateReservationRequest;
import com.billetterie.billetterie_api.dto.response.AnnulationResponse;
import com.billetterie.billetterie_api.dto.response.PageResponse;
import com.billetterie.billetterie_api.dto.response.ReservationResponse;
import com.billetterie.billetterie_api.dto.request.UpdateReservationRequest;
import com.billetterie.billetterie_api.entity.Client;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import com.billetterie.billetterie_api.entity.Statut;
import com.billetterie.billetterie_api.entity.Tarif;
import com.billetterie.billetterie_api.exception.EvenementNotFoundException;
import com.billetterie.billetterie_api.exception.EvenementPasseException;
import com.billetterie.billetterie_api.exception.PlaceDejaReserveeException;
import com.billetterie.billetterie_api.exception.PlaceHorsSalleException;
import com.billetterie.billetterie_api.exception.PlaceNotFoundException;
import com.billetterie.billetterie_api.exception.ReservationAnnulationImpossibleException;
import com.billetterie.billetterie_api.exception.ReservationExpireeException;
import com.billetterie.billetterie_api.exception.ReservationNonSupprimableException;
import com.billetterie.billetterie_api.exception.ReservationNotFoundException;
import com.billetterie.billetterie_api.exception.StatutReservationIncompatibleException;
import com.billetterie.billetterie_api.exception.TarifIntrouvableException;
import com.billetterie.billetterie_api.mapper.ReservationMapper;
import com.billetterie.billetterie_api.repository.ClientRepository;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.HistoriqueStatutRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.StatutRepository;
import com.billetterie.billetterie_api.repository.TarifRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des réservations.
 *
 * COUCHE       : Service
 * RÔLE         : gérer F4, F9 à F16 et exposer la vérification utilisée par F16.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Réservation/Client/Place/Statut/Tarif, Clock, mapper.
 * UTILISÉ PAR  : ReservationController.
 * NE FAIT PAS  : le HTTP.
 */
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final int TAILLE_MAX = 100;
    private static final Sort TRI_DEFAUT = Sort.by(Sort.Order.desc("dateCreation"));

    private final ReservationRepository reservationRepository;
    private final ReservationPlaceRepository reservationPlaceRepository;
    private final ClientRepository clientRepository;
    private final EvenementRepository evenementRepository;
    private final PlaceRepository placeRepository;
    private final StatutRepository statutRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final TarifRepository tarifRepository;
    private final ReservationMapper reservationMapper;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                               ReservationPlaceRepository reservationPlaceRepository,
                               ClientRepository clientRepository, EvenementRepository evenementRepository,
                               PlaceRepository placeRepository, StatutRepository statutRepository,
                               HistoriqueStatutRepository historiqueStatutRepository,
                               TarifRepository tarifRepository, ReservationMapper reservationMapper,
                               Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationPlaceRepository = reservationPlaceRepository;
        this.clientRepository = clientRepository;
        this.evenementRepository = evenementRepository;
        this.placeRepository = placeRepository;
        this.statutRepository = statutRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.tarifRepository = tarifRepository;
        this.reservationMapper = reservationMapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F4, F9, F10
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /** Retourne une page de réservations, triée et limitée à 100 éléments (F9). */
    public PageResponse<ReservationResponse> findAll(Pageable pageable) {
        Pageable normalise = normaliser(pageable);
        Page<Reservation> page = reservationRepository.findAll(normalise);
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    // ── findByEmail() ────────────────────────────────────────────────────────

    /** Retrouve les réservations d'un email, sans tenir compte de la casse (F10). */
    public List<ReservationResponse> findByEmail(String email) {
        return reservationRepository.findAllByClient_EmailIgnoreCaseOrderByDateCreationDesc(email).stream()
                .map(this::toResponse)
                .toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Retourne une réservation par identifiant.
     *
     * @throws ReservationNotFoundException si aucune réservation ne porte cet identifiant.
     */
    public ReservationResponse findById(Integer idReservation) {
        return toResponse(requireReservation(idReservation));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée une réservation PROVISOIRE en appliquant F11, F12 et F13. Modifie la base.
     *
     * @throws PlaceDejaReserveeException si la place est déjà réservée pour cet événement.
     *
     * @throws EvenementPasseException si l’événement est déjà passé.
     */
    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        // ── Étape 1/5 : résoudre l'événement et la place ─────────────────────
        Evenement evenement = evenementRepository.findById(request.idEvenement())
                .orElseThrow(() -> new EvenementNotFoundException(request.idEvenement()));
        Place place = placeRepository.findFirstByRangAndNumero(request.rang(), request.numero())
                .orElseThrow(() -> new PlaceNotFoundException(request.rang()));

        // ── Étape 2/5 : appliquer les règles métier ──────────────────────────
        if (!evenement.getDateHeure().isAfter(LocalDateTime.now(clock))) {
            throw new EvenementPasseException(request.idEvenement());
        }
        if (!place.getSalle().getIdSalle().equals(evenement.getSalle().getIdSalle())) {
            throw new PlaceHorsSalleException(request.numero());
        }
        if (reservationPlaceRepository.existsByReservation_Evenement_IdEvenementAndRangAndNumero(
                request.idEvenement(), request.rang(), request.numero())) {
            throw new PlaceDejaReserveeException(request.numero());
        }

        // ── Étape 3/5 : enregistrer le client, la réservation et son histor… ─
        Client client = clientRepository.findByEmailIgnoreCase(request.email())
                .orElseGet(() -> clientRepository.saveAndFlush(new Client(
                        request.nom().trim(), request.prenom().trim(), request.email().trim())));
        Reservation reservation = new Reservation(LocalDateTime.now(clock), client, evenement);
        reservation.ajouterPlace(new ReservationPlace(
                reservation, request.rang(), request.numero(), request.idEvenement()));
        Statut provisoire = statut("PROVISOIRE");
        reservation.ajouterHistorique(new HistoriqueStatut(reservation, provisoire,
                reservation.getDateCreation()));
        try {
            Reservation enregistree = reservationRepository.saveAndFlush(reservation);
            return toResponse(enregistree);
        } catch (DataIntegrityViolationException ex) {
            // L'unicité en base est la garantie finale contre les réservations concurrentes (F11).
            throw new PlaceDejaReserveeException(request.numero());
        }
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Modifie uniquement les informations client. Modifie la base.
     *
     * @throws ReservationNotFoundException si aucune réservation ne porte cet identifiant.
     */
    @Transactional
    public ReservationResponse update(Integer idReservation, UpdateReservationRequest request) {
        Reservation reservation = requireReservation(idReservation);
        reservation.getClient().modifier(request.nom().trim(), request.prenom().trim(),
                request.email().trim());
        return toResponse(reservation);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Supprime une réservation provisoire ou annulée. Modifie la base.
     *
     * @throws ReservationNotFoundException si aucune réservation ne porte cet identifiant.
     *
     * @throws ReservationNonSupprimableException si la réservation est payée.
     */
    @Transactional
    public void delete(Integer idReservation) {
        Reservation reservation = requireReservation(idReservation);
        String statut = statutCourant(reservation);
        if (!"PROVISOIRE".equalsIgnoreCase(statut) && !"ANNULEE".equalsIgnoreCase(statut)) {
            throw new ReservationNonSupprimableException(idReservation);
        }
        reservationRepository.delete(reservation);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F14
    // ═════════════════════════════════════════════════════════════════════════

    // ── payer() ──────────────────────────────────────────────────────────────

    /**
     * Confirme le paiement d'une réservation provisoire dans les 15 minutes. Modifie la base.
     *
     * @throws StatutReservationIncompatibleException si la réservation n’est pas PROVISOIRE.
     *
     * @throws ReservationExpireeException si le délai de quinze minutes est dépassé.
     */
    @Transactional
    public ReservationResponse payer(Integer idReservation) {
        // ── Étape 1/4 : charger la réservation et son statut ─────────────────
        Reservation reservation = requireReservation(idReservation);
        String statut = statutCourant(reservation);
        if (!"PROVISOIRE".equalsIgnoreCase(statut)) {
            throw new StatutReservationIncompatibleException(idReservation);
        }

        // ── Étape 2/4 : contrôler le délai ───────────────────────────────────
        LocalDateTime maintenant = LocalDateTime.now(clock);
        if (maintenant.isAfter(reservation.getDateCreation().plusMinutes(15))) {
            throw new ReservationExpireeException(idReservation);
        }

        // ── Étape 3/4 : appliquer le tarif et tracer le paiement ─────────────
        String categorie = categorieDe(reservation);
        Tarif tarif = tarifRepository.findByEvenement_IdEvenementAndCategorieIgnoreCase(
                        reservation.getEvenement().getIdEvenement(), categorie)
                .orElseThrow(() -> new TarifIntrouvableException(categorie));
        reservation.getPlaces().getFirst().setPrixApplique(tarif.getPrix());
        reservation.enregistrerPaiement(maintenant);
        // Décalage d'un millième de seconde : le statut PAYEE doit rester le plus récent
        // même si la base tronque les nanosecondes de date_statut.
        reservation.ajouterHistorique(new HistoriqueStatut(
                reservation, statut("PAYEE"), maintenant.plusNanos(1_000_000)));

        // ── Étape 4/4 : convertir en réponse ─────────────────────────────────
        return toResponse(reservation);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F15
    // ═════════════════════════════════════════════════════════════════════════

    // ── annuler() ────────────────────────────────────────────────────────────

    /**
     * Annule une réservation et calcule le remboursement selon la distance à l'événement (F15).
     *
     * @throws ReservationNotFoundException si aucune réservation ne porte cet identifiant.
     *
     * @throws ReservationAnnulationImpossibleException si la réservation est déjà annulée.
     */
    @Transactional
    public AnnulationResponse annuler(Integer idReservation) {
        // ── Étape 1/4 : charger et contrôler le statut ───────────────────────
        Reservation reservation = requireReservation(idReservation);
        String statut = statutCourant(reservation);
        boolean statutAcceptable = "PAYEE".equalsIgnoreCase(statut) || "PROVISOIRE".equalsIgnoreCase(statut);
        boolean dejaAnnulee = historiqueStatutRepository
                .existsByReservation_IdReservationAndStatut_LibelleIgnoreCase(idReservation, "ANNULEE");
        if (!statutAcceptable || dejaAnnulee) {
            throw new ReservationAnnulationImpossibleException(idReservation);
        }

        // ── Étape 2/4 : choisir le taux de remboursement ─────────────────────
        LocalDateTime maintenant = LocalDateTime.now(clock);
        long heures = ChronoUnit.HOURS.between(
                maintenant, reservation.getEvenement().getDateHeure());
        BigDecimal taux = heures > 48 ? BigDecimal.ONE
                : heures > 24 ? new BigDecimal("0.5") : BigDecimal.ZERO;

        // ── Étape 3/4 : calculer, tracer et libérer la place ─────────────────
        BigDecimal total = reservation.getPlaces().stream()
                .map(place -> place.getPrixApplique() == null ? BigDecimal.ZERO : place.getPrixApplique())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal montant = total.multiply(taux).setScale(2, RoundingMode.HALF_UP);
        reservation.setMontantRembourse(montant);
        // Décalage d'un millième de seconde : le statut ANNULEE doit rester le plus récent,
        // y compris si le paiement et l'annulation arrivent dans la même milliseconde.
        LocalDateTime dernierStatut = historiqueStatutRepository
                .findTopByReservation_IdReservationOrderByDateStatutDesc(idReservation)
                .map(HistoriqueStatut::getDateStatut)
                .orElse(maintenant);
        LocalDateTime base = dernierStatut.isAfter(maintenant) ? dernierStatut : maintenant;
        LocalDateTime dateAnnulation = base.plusNanos(1_000_000);
        reservation.ajouterHistorique(new HistoriqueStatut(
                reservation, statut("ANNULEE"), dateAnnulation));

        // ── Étape 4/4 : convertir en réponse ─────────────────────────────────
        return new AnnulationResponse(idReservation, "ANNULEE", montant, dateAnnulation);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireReservation() ─────────────────────────────────────────────────

    /** Charge une réservation ou lève l'erreur 404. */
    private Reservation requireReservation(Integer idReservation) {
        return reservationRepository.findById(idReservation)
                .orElseThrow(() -> new ReservationNotFoundException(idReservation));
    }

    // ── statut() ─────────────────────────────────────────────────────────────

    /** Résout un statut de la table de référence. */
    private Statut statut(String libelle) {
        return statutRepository.findByLibelleIgnoreCase(libelle)
                .orElseThrow(() -> new IllegalStateException("Statut manquant : " + libelle));
    }

    // ── statutCourant() ──────────────────────────────────────────────────────

    /** Retourne le libellé du statut le plus récent. */
    public String statutCourant(Reservation reservation) {
        return historiqueStatutRepository
                .findTopByReservation_IdReservationOrderByDateStatutDesc(reservation.getIdReservation())
                .map(historique -> historique.getStatut().getLibelle())
                .orElse("PROVISOIRE");
    }

    // ── categorieDe() ────────────────────────────────────────────────────────

    /** Retrouve la catégorie de la place de la réservation. */
    private String categorieDe(Reservation reservation) {
        ReservationPlace place = reservation.getPlaces().getFirst();
        return placeRepository.findBySalle_IdSalleAndRangAndNumero(
                        reservation.getEvenement().getSalle().getIdSalle(),
                        place.getRang(), place.getNumero())
                .map(p -> p.getCategorie().getLibelle())
                .orElseThrow(() -> new TarifIntrouvableException(place.getRang()));
    }

    // ── toResponse() ─────────────────────────────────────────────────────────

    /** Assemble le DTO d'une réservation en récupérant son statut et sa catégorie. */
    private ReservationResponse toResponse(Reservation reservation) {
        return reservationMapper.toResponse(reservation, statutCourant(reservation), categorieDe(reservation));
    }

    // ── normaliser() ─────────────────────────────────────────────────────────

    /** Plafonne la taille de page et applique le tri décroissant par défaut. */
    private Pageable normaliser(Pageable pageable) {
        int taille = Math.min(pageable.getPageSize(), TAILLE_MAX);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : TRI_DEFAUT;
        return PageRequest.of(pageable.getPageNumber(), taille, sort);
    }
}

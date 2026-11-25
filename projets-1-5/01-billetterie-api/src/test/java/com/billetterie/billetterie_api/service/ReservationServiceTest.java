package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.dto.request.CreateReservationRequest;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import com.billetterie.billetterie_api.exception.EvenementPasseException;
import com.billetterie.billetterie_api.exception.PlaceDejaReserveeException;
import com.billetterie.billetterie_api.exception.PlaceHorsSalleException;
import com.billetterie.billetterie_api.mapper.ReservationMapper;
import com.billetterie.billetterie_api.repository.ClientRepository;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.HistoriqueStatutRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.StatutRepository;
import com.billetterie.billetterie_api.repository.TarifRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires des règles métier de réservation.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires des règles métier de réservation.
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : horloge fixée au 20/11/2026, repositories simulés.
 * CAS COUVERTS : F11, F12, F13.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC);

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationPlaceRepository reservationPlaceRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private EvenementRepository evenementRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private StatutRepository statutRepository;
    @Mock private HistoriqueStatutRepository historiqueStatutRepository;
    @Mock private TarifRepository tarifRepository;

    private ReservationService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationRepository, reservationPlaceRepository,
                clientRepository, evenementRepository, placeRepository, statutRepository,
                historiqueStatutRepository, tarifRepository, new ReservationMapper(), CLOCK);
    }

    // ── request() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour request().
     */

    private CreateReservationRequest request() {
        return new CreateReservationRequest("Dupont", "Jean", "jean@example.com", 1, "A", 12);
    }

    // ── evenement() ──────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour evenement().
     */

    private Evenement evenement(LocalDateTime date) {
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        Evenement evenement = new Evenement(date,
                new Spectacle("Concert Jazz", TypeSpectacle.CONCERT, true), salle);
        ReflectionTestUtils.setField(evenement, "idEvenement", 1);
        return evenement;
    }

    // ── place() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour place().
     */

    private Place place(Salle salle) {
        return new Place("A", 12, salle, new Categorie("ORCHESTRE"));
    }

    // ── create_throwsWhenEvenementPasse() ────────────────────────────────────

    @Test
    @DisplayName("Un événement passé ne peut plus être réservé")
    void create_throwsWhenEvenementPasse() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(evenementRepository.findById(1)).thenReturn(Optional.of(
                evenement(LocalDateTime.of(2026, 11, 19, 20, 0))));
        when(placeRepository.findFirstByRangAndNumero("A", 12)).thenReturn(Optional.of(
                place(new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120))));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(EvenementPasseException.class);
    }

    // ── create_throwsWhenPlaceHorsSalle() ────────────────────────────────────

    @Test
    @DisplayName("Une place appartenant à une autre salle est refusée")
    void create_throwsWhenPlaceHorsSalle() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(evenementRepository.findById(1)).thenReturn(Optional.of(
                evenement(LocalDateTime.of(2026, 12, 20, 20, 0))));
        Salle autreSalle = new Salle("Petit Hall", "8 avenue des Arts", "Bouaké", 40);
        ReflectionTestUtils.setField(autreSalle, "idSalle", 2);
        when(placeRepository.findFirstByRangAndNumero("A", 12))
                .thenReturn(Optional.of(place(autreSalle)));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(PlaceHorsSalleException.class);
    }

    // ── create_throwsWhenPlaceDejaReservee() ─────────────────────────────────

    @Test
    @DisplayName("Une place déjà réservée pour l'événement est refusée")
    void create_throwsWhenPlaceDejaReservee() {
        // ── Préparation ──────────────────────────────────────────────────────
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        when(evenementRepository.findById(1)).thenReturn(Optional.of(
                evenement(LocalDateTime.of(2026, 12, 20, 20, 0))));
        when(placeRepository.findFirstByRangAndNumero("A", 12))
                .thenReturn(Optional.of(place(salle)));
        when(reservationPlaceRepository
                .existsByReservation_Evenement_IdEvenementAndRangAndNumero(1, "A", 12))
                .thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(PlaceDejaReserveeException.class);
    }
}

package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.dto.response.EvenementResponse;
import com.billetterie.billetterie_api.dto.response.RemplissageResponse;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Client;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import com.billetterie.billetterie_api.exception.PeriodeInvalideException;
import com.billetterie.billetterie_api.exception.SalleSansPlaceException;
import com.billetterie.billetterie_api.mapper.CsvExportMapper;
import com.billetterie.billetterie_api.mapper.EvenementMapper;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import com.billetterie.billetterie_api.repository.SpectacleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de la recherche par période (F8) et du taux de remplissage (F18).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de la recherche par période (F8) et du taux de remplissage (F18).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : horloge fixée au 20/11/2026, repositories simulés, mappers réels.
 * CAS COUVERTS : F8 (4 cas de bornes + période inversée) et F18 (0 %, 100 %, mixte, salle vide).
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class EvenementServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC);

    @Mock private EvenementRepository evenementRepository;
    @Mock private SalleRepository salleRepository;
    @Mock private SpectacleRepository spectacleRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationPlaceRepository reservationPlaceRepository;

    private EvenementService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new EvenementService(evenementRepository, salleRepository, spectacleRepository,
                placeRepository, reservationRepository, reservationPlaceRepository,
                new EvenementMapper(CLOCK), new CsvExportMapper(), CLOCK);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  F8 — RECHERCHE PAR PÉRIODE
    // ═════════════════════════════════════════════════════════════════════════

    // ── evenement() ──────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour evenement().
     */

    private Evenement evenement(int id, LocalDateTime date) {
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        Evenement evenement = new Evenement(date,
                new Spectacle("Concert " + id, TypeSpectacle.CONCERT, true), salle);
        ReflectionTestUtils.setField(evenement, "idEvenement", id);
        return evenement;
    }

    // ── findBetween_sansBorne() ──────────────────────────────────────────────

    @Test
    @DisplayName("Sans borne, la liste complète est renvoyée")
    void findBetween_sansBorne() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(evenementRepository.findAllByOrderByDateHeureAsc())
                .thenReturn(List.of(evenement(1, LocalDateTime.of(2026, 12, 1, 20, 0))));

        // ── Action ───────────────────────────────────────────────────────────
        List<EvenementResponse> resultat = service.findBetween(null, null);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).extracting(EvenementResponse::idEvenement).containsExactly(1);
        verify(evenementRepository).findAllByOrderByDateHeureAsc();
    }

    // ── findBetween_debutSeul() ──────────────────────────────────────────────

    @Test
    @DisplayName("Une borne de début suffit")
    void findBetween_debutSeul() {
        // ── Préparation ──────────────────────────────────────────────────────
        LocalDateTime du = LocalDateTime.of(2026, 12, 1, 0, 0);
        when(evenementRepository.findAllByDateHeureGreaterThanEqualOrderByDateHeureAsc(du))
                .thenReturn(List.of(evenement(1, du)));

        // ── Action ───────────────────────────────────────────────────────────
        List<EvenementResponse> resultat = service.findBetween(du, null);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).extracting(EvenementResponse::idEvenement).containsExactly(1);
    }

    // ── findBetween_finSeule() ───────────────────────────────────────────────

    @Test
    @DisplayName("Une borne de fin suffit")
    void findBetween_finSeule() {
        // ── Préparation ──────────────────────────────────────────────────────
        LocalDateTime au = LocalDateTime.of(2026, 12, 31, 0, 0);
        when(evenementRepository.findAllByDateHeureLessThanEqualOrderByDateHeureAsc(au))
                .thenReturn(List.of(evenement(2, au)));

        // ── Action ───────────────────────────────────────────────────────────
        List<EvenementResponse> resultat = service.findBetween(null, au);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).extracting(EvenementResponse::idEvenement).containsExactly(2);
    }

    // ── findBetween_deuxBornesInclusives() ───────────────────────────────────

    @Test
    @DisplayName("Les deux bornes sont inclusives")
    void findBetween_deuxBornesInclusives() {
        // ── Préparation ──────────────────────────────────────────────────────
        LocalDateTime du = LocalDateTime.of(2026, 12, 1, 0, 0);
        LocalDateTime au = LocalDateTime.of(2026, 12, 31, 0, 0);
        when(evenementRepository.findAllByDateHeureBetweenOrderByDateHeureAsc(du, au))
                .thenReturn(List.of(evenement(1, du), evenement(2, au)));

        // ── Action ───────────────────────────────────────────────────────────
        List<EvenementResponse> resultat = service.findBetween(du, au);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).extracting(EvenementResponse::idEvenement).containsExactly(1, 2);
    }

    // ── findBetween_throwsWhenPeriodeInversee() ──────────────────────────────

    @Test
    @DisplayName("Une période inversée est refusée")
    void findBetween_throwsWhenPeriodeInversee() {
        // ── Préparation ──────────────────────────────────────────────────────
        LocalDateTime du = LocalDateTime.of(2026, 12, 31, 0, 0);
        LocalDateTime au = LocalDateTime.of(2026, 12, 1, 0, 0);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.findBetween(du, au))
                .isInstanceOf(PeriodeInvalideException.class);
        verify(evenementRepository, never()).findAllByDateHeureBetweenOrderByDateHeureAsc(du, au);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  F18 — TAUX DE REMPLISSAGE
    // ═════════════════════════════════════════════════════════════════════════

    // ── salleAvecId() ────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour salleAvecId().
     */

    private Salle salleAvecId() {
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        return salle;
    }

    // ── place() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour place().
     */

    private Place place(Salle salle, String categorie, int numero) {
        Place place = new Place("A", numero, salle, new Categorie(categorie));
        ReflectionTestUtils.setField(place, "idPlace", numero);
        return place;
    }

    // ── placeReservee() ──────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour placeReservee().
     */

    private ReservationPlace placeReservee(int numero) {
        Reservation reservation = new Reservation(LocalDateTime.now(CLOCK),
                new Client("Dupont", "Jean", "jean@example.com"),
                new Evenement(LocalDateTime.of(2026, 12, 20, 20, 0),
                        new Spectacle("Concert", TypeSpectacle.CONCERT, true), salleAvecId()));
        ReflectionTestUtils.setField(reservation, "idReservation", 1);
        return new ReservationPlace(reservation, "A", numero, 1);
    }

    // ── preparerEvenementEtPlaces() ──────────────────────────────────────────

    /**
     * Prépare la donnée de test pour preparerEvenementEtPlaces().
     */

    private void preparerEvenementEtPlaces(List<Place> places) {
        when(evenementRepository.findById(1))
                .thenReturn(Optional.of(evenement(1, LocalDateTime.of(2026, 12, 20, 20, 0))));
        when(placeRepository.findAllBySalle_IdSalleOrderByRangAscNumeroAsc(1)).thenReturn(places);
    }

    // ── remplissage_sansReservation() ────────────────────────────────────────

    @Test
    @DisplayName("Un événement sans réservation affiche 0 %")
    void remplissage_sansReservation() {
        // ── Préparation ──────────────────────────────────────────────────────
        preparerEvenementEtPlaces(List.of(place(salleAvecId(), "ORCHESTRE", 1)));
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PROVISOIRE"))
                .thenReturn(List.of());
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PAYEE"))
                .thenReturn(List.of());

        // ── Action ───────────────────────────────────────────────────────────
        RemplissageResponse resultat = service.findRemplissage(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.placesTotales()).isEqualTo(1);
        assertThat(resultat.taux()).isEqualByComparingTo("0.00");
        assertThat(resultat.parCategorie()).singleElement()
                .satisfies(d -> assertThat(d.taux()).isEqualByComparingTo("0.00"));
    }

    // ── remplissage_centPourCent() ───────────────────────────────────────────

    @Test
    @DisplayName("Une place réservée affiche 100 %")
    void remplissage_centPourCent() {
        // ── Préparation ──────────────────────────────────────────────────────
        Salle salle = salleAvecId();
        preparerEvenementEtPlaces(List.of(place(salle, "ORCHESTRE", 1)));
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PROVISOIRE"))
                .thenReturn(List.of());
        ReservationPlace payee = placeReservee(1);
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PAYEE"))
                .thenReturn(List.of(payee));
        when(placeRepository.findBySalle_IdSalleAndRangAndNumero(1, "A", 1))
                .thenReturn(Optional.of(place(salle, "ORCHESTRE", 1)));

        // ── Action ───────────────────────────────────────────────────────────
        RemplissageResponse resultat = service.findRemplissage(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.payees()).isEqualTo(1);
        assertThat(resultat.taux()).isEqualByComparingTo("100.00");
    }

    // ── remplissage_statutsMelanges() ────────────────────────────────────────

    @Test
    @DisplayName("Les statuts provisoire et payé sont comptés séparément")
    void remplissage_statutsMelanges() {
        // ── Préparation ──────────────────────────────────────────────────────
        Salle salle = salleAvecId();
        preparerEvenementEtPlaces(List.of(
                place(salle, "ORCHESTRE", 1), place(salle, "ORCHESTRE", 2),
                place(salle, "BALCON", 3), place(salle, "BALCON", 4)));
        ReservationPlace provisoire = placeReservee(1);
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PROVISOIRE"))
                .thenReturn(List.of(provisoire));
        ReservationPlace payee = placeReservee(3);
        when(reservationPlaceRepository.findPlacesByEvenementAndStatut(1, "PAYEE"))
                .thenReturn(List.of(payee));
        when(placeRepository.findBySalle_IdSalleAndRangAndNumero(1, "A", 1))
                .thenReturn(Optional.of(place(salle, "ORCHESTRE", 1)));
        when(placeRepository.findBySalle_IdSalleAndRangAndNumero(1, "A", 3))
                .thenReturn(Optional.of(place(salle, "BALCON", 3)));

        // ── Action ───────────────────────────────────────────────────────────
        RemplissageResponse resultat = service.findRemplissage(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.provisoires()).isEqualTo(1);
        assertThat(resultat.payees()).isEqualTo(1);
        assertThat(resultat.taux()).isEqualByComparingTo("50.00");
        assertThat(resultat.parCategorie()).hasSize(2);
    }

    // ── remplissage_throwsWhenSalleSansPlace() ───────────────────────────────

    @Test
    @DisplayName("Une salle sans place est refusée avec un 409")
    void remplissage_throwsWhenSalleSansPlace() {
        // ── Préparation ──────────────────────────────────────────────────────
        preparerEvenementEtPlaces(List.of());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.findRemplissage(1))
                .isInstanceOf(SalleSansPlaceException.class);
        verify(reservationPlaceRepository, never()).findPlacesByEvenementAndStatut(eq(1), anyString());
    }
}

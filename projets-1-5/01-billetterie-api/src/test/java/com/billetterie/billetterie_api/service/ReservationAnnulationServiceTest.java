package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.dto.response.AnnulationResponse;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Client;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.ReservationPlace;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.entity.Statut;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import com.billetterie.billetterie_api.exception.ReservationAnnulationImpossibleException;
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
 * Tests unitaires de l'annulation avec remboursement (F15).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de l'annulation avec remboursement (F15).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : horloge fixée au 20/11/2026 10:00 UTC, événement à distance variable.
 * CAS COUVERTS : > 48 h (100 %), 24–48 h (50 %), < 24 h (0 %), double annulation refusée.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ReservationAnnulationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 20, 10, 0);

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
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationRepository, reservationPlaceRepository,
                clientRepository, evenementRepository, placeRepository, statutRepository,
                historiqueStatutRepository, tarifRepository, new ReservationMapper(), CLOCK);
    }

    // ── reservation() ────────────────────────────────────────────────────────

    /**
     * Prépare un reservationannulation de test avec des valeurs nominales.
     */

    private Reservation reservation(LocalDateTime dateEvenement) {
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        Evenement evenement = new Evenement(dateEvenement,
                new Spectacle("Concert", TypeSpectacle.CONCERT, true), salle);
        ReflectionTestUtils.setField(evenement, "idEvenement", 1);
        Reservation reservation = new Reservation(MAINTENANT,
                new Client("Dupont", "Jean", "jean@example.com"), evenement);
        ReflectionTestUtils.setField(reservation, "idReservation", 1);
        return reservation;
    }

    // ── preparer() ───────────────────────────────────────────────────────────

    /**
     * Prépare un reservationannulation pour le scénario de test.
     */

    private void preparer(Reservation reservation, String statutCourant, boolean dejaAnnulee) {
        preparer(reservation, statutCourant, dejaAnnulee, true);
    }

    // ── preparer() ───────────────────────────────────────────────────────────

    /**
     * Prépare un reservationannulation pour le scénario de test.
     */

    private void preparer(Reservation reservation, String statutCourant, boolean dejaAnnulee,
                          boolean annulable) {
        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(historiqueStatutRepository
                .findTopByReservation_IdReservationOrderByDateStatutDesc(1))
                .thenReturn(Optional.of(new HistoriqueStatut(
                        reservation, new Statut(statutCourant), MAINTENANT.minusHours(1))));
        when(historiqueStatutRepository
                .existsByReservation_IdReservationAndStatut_LibelleIgnoreCase(1, "ANNULEE"))
                .thenReturn(dejaAnnulee);
        if (annulable) {
            when(statutRepository.findByLibelleIgnoreCase("ANNULEE"))
                    .thenReturn(Optional.of(new Statut("ANNULEE")));
        }
    }

    // ── ajouterPlace() ───────────────────────────────────────────────────────

    /**
     * Prépare le contexte nécessaire à l’opération ajouterPlace().
     */

    private void ajouterPlace(Reservation reservation, String prix) {
        ReservationPlace place = new ReservationPlace(
                reservation, "A", 1, reservation.getEvenement().getIdEvenement());
        place.setPrixApplique(new BigDecimal(prix));
        reservation.ajouterPlace(place);
    }

    // ── annuler_auDelai48h() ─────────────────────────────────────────────────

    @Test
    @DisplayName("Au-delà de 48 h, le remboursement est intégral")
    void annuler_auDelai48h() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = reservation(MAINTENANT.plusHours(72));
        ajouterPlace(reservation, "12.50");
        preparer(reservation, "PAYEE", false);

        // ── Action ───────────────────────────────────────────────────────────
        AnnulationResponse resultat = service.annuler(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.statut()).isEqualTo("ANNULEE");
        assertThat(resultat.montantRembourse()).isEqualByComparingTo("12.50");
        assertThat(reservation.getMontantRembourse()).isEqualByComparingTo("12.50");
        assertThat(reservation.getHistorique()).hasSize(1);
        assertThat(resultat.dateAnnulation()).isAfter(MAINTENANT);
    }

    // ── annuler_entre24hEt48h() ──────────────────────────────────────────────

    @Test
    @DisplayName("Entre 24 h et 48 h, la moitié est remboursée")
    void annuler_entre24hEt48h() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = reservation(MAINTENANT.plusHours(47));
        ajouterPlace(reservation, "20.00");
        preparer(reservation, "PAYEE", false);

        // ── Action ───────────────────────────────────────────────────────────
        AnnulationResponse resultat = service.annuler(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.montantRembourse()).isEqualByComparingTo("10.00");
    }

    // ── annuler_enDeça24h() ──────────────────────────────────────────────────

    @Test
    @DisplayName("En deçà de 24 h, aucun remboursement n'est dû")
    void annuler_enDeça24h() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = reservation(MAINTENANT.plusHours(19));
        ajouterPlace(reservation, "20.00");
        preparer(reservation, "PAYEE", false);

        // ── Action ───────────────────────────────────────────────────────────
        AnnulationResponse resultat = service.annuler(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.montantRembourse()).isEqualByComparingTo("0.00");
    }

    // ── annuler_throwsWhenDejaAnnulee() ──────────────────────────────────────

    @Test
    @DisplayName("Une réservation déjà annulée ne peut pas l'être une seconde fois")
    void annuler_throwsWhenDejaAnnulee() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = reservation(MAINTENANT.plusHours(72));
        preparer(reservation, "ANNULEE", true, false);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.annuler(1))
                .isInstanceOf(ReservationAnnulationImpossibleException.class);
    }
}

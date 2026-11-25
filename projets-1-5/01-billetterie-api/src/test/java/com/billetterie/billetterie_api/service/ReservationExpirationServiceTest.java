package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.entity.Client;
import com.billetterie.billetterie_api.entity.Evenement;
import com.billetterie.billetterie_api.entity.HistoriqueStatutId;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import com.billetterie.billetterie_api.entity.Statut;
import com.billetterie.billetterie_api.repository.HistoriqueStatutRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.StatutRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de la tâche d'expiration (F16).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de la tâche d'expiration (F16).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : horloge fixée ; repositories simulés.
 * CAS COUVERTS : expiration journalisée, pas de double journalisation.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ReservationExpirationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC);

    @Mock private ReservationRepository reservationRepository;
    @Mock private HistoriqueStatutRepository historiqueStatutRepository;

    // ── service() ────────────────────────────────────────────────────────────

    /**
     * Prépare un reservationexpiration de test avec des valeurs nominales.
     */

    @Mock private StatutRepository statutRepository;

    private ReservationExpirationService service() {
        return new ReservationExpirationService(reservationRepository,
                historiqueStatutRepository, statutRepository, CLOCK);
    }

    // ── libererReservationsExpirees_ajouteHistorique() ───────────────────────

    @Test
    @DisplayName("Une réservation expirée passe en ANNULEE et est journalisée")
    void libererReservationsExpirees_ajouteHistorique() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = new Reservation(LocalDateTime.of(2026, 11, 20, 9, 40),
                new Client("Dupont", "Jean", "jean@example.com"),
                new Evenement(LocalDateTime.of(2026, 12, 20, 20, 0),
                        new Spectacle("Concert Jazz", TypeSpectacle.CONCERT, true),
                        new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120)));
        ReflectionTestUtils.setField(reservation, "idReservation", 7);
        Statut annulee = new Statut("ANNULEE");
        ReflectionTestUtils.setField(annulee, "idStatut", 3);
        when(reservationRepository.findProvisoiresExpirees(
                LocalDateTime.of(2026, 11, 20, 9, 45))).thenReturn(List.of(reservation));
        when(statutRepository.findByLibelleIgnoreCase("ANNULEE")).thenReturn(Optional.of(annulee));
        when(historiqueStatutRepository.existsById(any(HistoriqueStatutId.class))).thenReturn(false);

        // ── Action ───────────────────────────────────────────────────────────
        service().libererReservationsExpirees();

        // ── Vérification ─────────────────────────────────────────────────────
        ArgumentCaptor<HistoriqueStatutId> captor =
                ArgumentCaptor.forClass(HistoriqueStatutId.class);
        verify(historiqueStatutRepository).existsById(captor.capture());
        assertThat(captor.getValue().getIdReservation()).isEqualTo(7);
        assertThat(reservation.getHistorique()).hasSize(1);
    }

    // ── libererReservationsExpirees_ignoreHistoriqueExistant() ───────────────

    @Test
    @DisplayName("Une réservation déjà annulée n'est pas journalisée deux fois")
    void libererReservationsExpirees_ignoreHistoriqueExistant() {
        // ── Préparation ──────────────────────────────────────────────────────
        Reservation reservation = new Reservation(LocalDateTime.of(2026, 11, 20, 9, 40),
                new Client("Dupont", "Jean", "jean@example.com"),
                new Evenement(LocalDateTime.of(2026, 12, 20, 20, 0),
                        new Spectacle("Concert Jazz", TypeSpectacle.CONCERT, true),
                        new Salle("Grand Hall", "12 rue des Arts", "Abidjan", 120)));
        ReflectionTestUtils.setField(reservation, "idReservation", 7);
        Statut annulee = new Statut("ANNULEE");
        ReflectionTestUtils.setField(annulee, "idStatut", 3);
        when(reservationRepository.findProvisoiresExpirees(any())).thenReturn(List.of(reservation));
        when(statutRepository.findByLibelleIgnoreCase("ANNULEE")).thenReturn(Optional.of(annulee));
        when(historiqueStatutRepository.existsById(any(HistoriqueStatutId.class))).thenReturn(true);

        // ── Action ───────────────────────────────────────────────────────────
        service().libererReservationsExpirees();

        // ── Vérification ─────────────────────────────────────────────────────
        verify(historiqueStatutRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
        assertThat(reservation.getHistorique()).isEmpty();
    }
}

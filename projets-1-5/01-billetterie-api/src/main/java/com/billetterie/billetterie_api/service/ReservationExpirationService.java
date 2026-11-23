package com.billetterie.billetterie_api.service;

import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.HistoriqueStatutId;
import com.billetterie.billetterie_api.entity.Reservation;
import com.billetterie.billetterie_api.entity.Statut;
import com.billetterie.billetterie_api.repository.HistoriqueStatutRepository;
import com.billetterie.billetterie_api.repository.ReservationRepository;
import com.billetterie.billetterie_api.repository.StatutRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tâche d'expiration des réservations provisoires (F16).
 *
 * COUCHE       : Service
 * RÔLE         : annuler et journaliser les réservations non payées depuis 15 minutes.
 * TRANSACTION  : chaque exécution est transactionnelle.
 * UTILISÉ PAR  : planificateur Spring (aucun contrôleur).
 * NE FAIT PAS  : de remboursement (F15) ni de HTTP.
 */
@Service
public class ReservationExpirationService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationService.class);

    private final ReservationRepository reservationRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final StatutRepository statutRepository;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR  (injection de dépendances)
    // ═════════════════════════════════════════════════════════════════════════

    public ReservationExpirationService(ReservationRepository reservationRepository,
                                        HistoriqueStatutRepository historiqueStatutRepository,
                                        StatutRepository statutRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.statutRepository = statutRepository;
        this.clock = clock;
    }

    // ── libererReservationsExpirees() ────────────────────────────────────────

    /**
     * Expire les réservations créées il y a plus de 15 minutes ; modifie la base.
     *
     * @throws IllegalStateException si le statut ANNULEE est absent de la base.
     */
    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F16
    // ═════════════════════════════════════════════════════════════════════════

    @Scheduled(fixedDelayString = "${billetterie.expiration.delai-ms:60000}")
    @Transactional
    public void libererReservationsExpirees() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(15);
        Statut annulee = statutRepository.findByLibelleIgnoreCase("ANNULEE")
                .orElseThrow(() -> new IllegalStateException("Statut ANNULEE manquant"));
        for (Reservation reservation : reservationRepository.findProvisoiresExpirees(limite)) {
            HistoriqueStatutId id = new HistoriqueStatutId(
                    reservation.getIdReservation(), annulee.getIdStatut());
            if (historiqueStatutRepository.existsById(id)) {
                continue;
            }
            reservation.ajouterHistorique(new HistoriqueStatut(
                    reservation, annulee, LocalDateTime.now(clock)));
            log.info("Réservation {} libérée après expiration", reservation.getIdReservation());
        }
    }
}

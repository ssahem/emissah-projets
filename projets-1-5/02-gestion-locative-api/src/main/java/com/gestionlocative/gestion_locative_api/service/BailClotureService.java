package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.HistoriqueBail;
import com.gestionlocative.gestion_locative_api.enums.MotifHistoriqueBail;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.HistoriqueBailRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clôture automatique des baux arrivés à terme (F16).
 *
 * COUCHE       : Service
 * RÔLE         : chaque nuit, passer en {@code TERMINE} les baux {@code EN_COURS}
 *                dont la date de fin est antérieure à aujourd'hui, et tracer
 *                chaque clôture dans {@code historique_bail}.
 * DÉPEND DE    : BailRepository, HistoriqueBailRepository et l'horloge applicative.
 * ACTIVATION   : {@code @EnableScheduling} dans SchedulingConfig.
 * NE FAIT PAS  : de HTTP ; aucune erreur n'est renvoyée au client.
 */
@Service
public class BailClotureService {

    private static final Logger log = LoggerFactory.getLogger(BailClotureService.class);

    private final BailRepository bailRepository;
    private final HistoriqueBailRepository historiqueBailRepository;
    private final Clock clock;

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public BailClotureService(BailRepository bailRepository,
                              HistoriqueBailRepository historiqueBailRepository,
                              Clock clock) {
        this.bailRepository = bailRepository;
        this.historiqueBailRepository = historiqueBailRepository;
        this.clock = clock;
    }

    // ── cloturerBauxEchus() ─────────────────────────────────────────────────

    /**
     * Clôture les baux échus et retourne leur nombre.
     * Exécutée chaque nuit à 01:00 UTC, avec la même horloge que les autres règles temporelles.
     *
     * @return le nombre de baux clôturés lors de cette exécution
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    @Transactional
    public int cloturerBauxEchus() {
        // ── Étape 1/3 : déterminer les baux concernés avec l'horloge applicative ──
        LocalDate aujourdHui = LocalDate.now(clock);
        List<Bail> echus = bailRepository.findByStatutAndDateFinBefore(
                StatutBail.EN_COURS, aujourdHui);

        // ── Étape 2/3 : clôturer et tracer chaque bail ──
        for (Bail bail : echus) {
            Integer idBail = bail.getIdBail();
            LocalDate fin = bail.getDateFin();
            historiqueBailRepository.saveAndFlush(new HistoriqueBail(
                    idBail, MotifHistoriqueBail.CLOTURE, aujourdHui, fin, fin));
            bail.cloturer(aujourdHui);
            log.info("Bail n°{} clôturé automatiquement (échéance du {})", idBail, fin);
        }

        // ── Étape 3/3 : rendre le compte pour la journalisation et les tests ──
        return echus.size();
    }
}

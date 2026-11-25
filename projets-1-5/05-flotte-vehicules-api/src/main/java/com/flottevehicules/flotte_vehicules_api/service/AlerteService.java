package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.response.AlerteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.AlertesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.entity.AlerteVehicule;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.repository.AlerteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alertes d'échéance d'entretien (F16) et consultation des alertes (A-02).
 *
 * COUCHE       : Service
 * DÉCLENCHEUR  : tâche planifiée chaque nuit, plus un endpoint manuel de démonstration.
 * RÈGLE        : seuil d'alerte à moins de 1 000 km ou 30 jours de l'échéance, ou dépassement.
 * TRAÇABILITÉ  : une échéance déjà signalée n'est pas signalée une seconde fois avant
 *                entretien.
 * RÔLE         : Alertes d'échéance d'entretien (F16) et consultation des alertes (A-02).
 * NE FAIT PAS  : de HTTP ni d'écriture dans les vues.
 */
@Service
@Transactional(readOnly = true)
public class AlerteService {

    private static final Logger log = LoggerFactory.getLogger(AlerteService.class);
    private static final String MOTIF_ECHEANCE = "ECHEANCE_ENTRETIEN";
    private static final int SEUIL_KM = 1000;
    private static final int SEUIL_JOURS = 30;

    private final AlerteRepository alerteRepository;
    private final VueRepository vueRepository;
    private final VehiculeRepository vehiculeRepository;
    private final SiteRepository siteRepository;
    private final Clock clock;

    public AlerteService(AlerteRepository alerteRepository, VueRepository vueRepository,
                         VehiculeRepository vehiculeRepository, SiteRepository siteRepository,
                         Clock clock) {
        this.alerteRepository = alerteRepository;
        this.vueRepository = vueRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.siteRepository = siteRepository;
        this.clock = clock;
    }


    // ── tacheQuotidienne() ───────────────────────────────────────────────────

    /**
     * Tâche nocturne F16 : génère les alertes ; l'expression cron vient de la
     * configuration.
     */

    @Scheduled(cron = "${flotte-vehicules.alertes-entretiens.cron}")
    @Transactional
    public void tacheQuotidienne() {
        List<AlertesParSiteResponse> alertes = genererAlertes();
        log.info("F16 : {} alerte(s) produite(s) sur {} site(s)",
                alertes.stream().mapToInt(a -> a.alertes().size()).sum(), alertes.size());
    }


    // ── genererAlertes() ─────────────────────────────────────────────────────

    /**
     * F16 : génère les alertes manquantes et les regroupe par site.
     */

    @Transactional
    public List<AlertesParSiteResponse> genererAlertes() {
        LocalDate aujourdHui = LocalDate.now(clock);
        String periode = aujourdHui.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        for (VueRepository.EcheanceRow echeance : vueRepository.toutesEcheances()) {
            if (!doitAlerter(echeance, aujourdHui)) {
                continue;
            }
            if (alerteRepository.existsByIdVehiculeAndMotifAndDateEcheance(
                    echeance.idVehicule(), MOTIF_ECHEANCE, echeance.dateProchaine())) {
                continue;
            }
            Vehicule vehicule = vehiculeRepository.findById(echeance.idVehicule()).orElse(null);
            if (vehicule == null) {
                continue;
            }
            int ecartKm = echeance.kmProchain() - echeance.kmActuel();
            int ecartJours = (int) ChronoUnit.DAYS.between(aujourdHui, echeance.dateProchaine());
            alerteRepository.save(new AlerteVehicule(
                    null, vehicule.getIdVehicule(), MOTIF_ECHEANCE, periode,
                    aujourdHui, echeance.dateProchaine(), ecartKm, ecartJours,
                    "Véhicule " + vehicule.getImmatriculation() + " : "
                            + (ecartKm <= 0 || ecartJours <= 0
                                ? "échéance d'entretien dépassée"
                                : "échéance d'entretien proche")));
        }
        return alertesParSite();
    }


    // ── lister() ─────────────────────────────────────────────────────────────

    /**
     * Consultation des alertes persistées, toutes ou filtrées par véhicule.
     */

    public List<AlerteResponse> lister(Integer idVehicule) {
        return (idVehicule == null
                ? alerteRepository.findAllByOrderByDateAlerteDescIdAlerteDesc()
                : alerteRepository.findAllByOrderByDateAlerteDescIdAlerteDesc().stream()
                        .filter(a -> a.getIdVehicule().equals(idVehicule)).toList())
                .stream().map(this::toResponse).toList();
    }


    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Consultation d'une alerte par identifiant.
     */

    public AlerteResponse findById(Integer id) {
        AlerteVehicule alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Alerte", id));
        return toResponse(alerte);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── doitAlerter() ────────────────────────────────────────────────────────

    /**
     * Réalise l’opération doitAlerter sur une alerte.
     */

    private boolean doitAlerter(VueRepository.EcheanceRow echeance, LocalDate aujourdHui) {
        if (echeance.dateProchaine() == null) {
            return false;
        }
        return echeance.kmActuel() >= echeance.kmProchain() - SEUIL_KM
                || !echeance.dateProchaine().isAfter(aujourdHui.plusDays(SEUIL_JOURS));
    }

    // ── alertesParSite() ─────────────────────────────────────────────────────

    /**
     * Réalise l’opération alertesParSite sur une alerte.
     */

    private List<AlertesParSiteResponse> alertesParSite() {
        Map<Integer, List<AlerteResponse>> groupes = new TreeMap<>();
        for (AlerteVehicule alerte : alerteRepository.findAllByOrderByDateAlerteDescIdAlerteDesc()) {
            Vehicule vehicule = vehiculeRepository.findById(alerte.getIdVehicule()).orElse(null);
            Integer idSite = vehicule == null ? null : vehicule.getIdSite();
            groupes.computeIfAbsent(idSite, site -> new ArrayList<>()).add(toResponse(alerte));
        }
        List<AlertesParSiteResponse> resultat = new ArrayList<>();
        for (Map.Entry<Integer, List<AlerteResponse>> entree : groupes.entrySet()) {
            String nomSite = entree.getKey() == null ? "Site inconnu" : siteRepository
                    .findById(entree.getKey()).map(Site::getNom).orElse("Site inconnu");
            resultat.add(new AlertesParSiteResponse(
                    entree.getKey(), nomSite,
                    entree.getValue().stream()
                            .sorted(Comparator.comparing(AlerteResponse::dateEcheance).reversed())
                            .toList()));
        }
        return resultat;
    }

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Convertit une alerte en DTO de réponse.
     */

    private AlerteResponse toResponse(AlerteVehicule alerte) {
        Vehicule vehicule = vehiculeRepository.findById(alerte.getIdVehicule()).orElse(null);
        String immatriculation = vehicule == null ? null : vehicule.getImmatriculation();
        String nomSite = null;
        if (vehicule != null) {
            nomSite = siteRepository.findById(vehicule.getIdSite()).map(Site::getNom).orElse(null);
        }
        return new AlerteResponse(
                alerte.getIdAlerte(), alerte.getIdVehicule(), immatriculation, nomSite,
                alerte.getIdAffectation(), alerte.getMotif(), alerte.getPeriode(),
                alerte.getDateAlerte(), alerte.getDateEcheance(), alerte.getEcartKm(),
                alerte.getEcartJours(), alerte.getDetail());
    }
}

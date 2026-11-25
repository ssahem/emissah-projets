package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.DetailKilometrageResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.KilometrageSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.SiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeSiteResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.SiteMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des sites (F1), parc d'un site (F6) et kilométrage mensuel (F19).
 *
 * COUCHE       : Service
 * RÔLE         : CRUD des sites (F1), parc d'un site (F6) et kilométrage mensuel (F19).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories métier, VueRepository en lecture seule, Clock, SiteMapper.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;
    private final VehiculeRepository vehiculeRepository;
    private final AffectationRepository affectationRepository;
    private final EntretienRepository entretienRepository;
    private final VueRepository vueRepository;
    private final SiteMapper mapper;
    private final Clock clock;

    public SiteService(SiteRepository siteRepository, VehiculeRepository vehiculeRepository,
                       AffectationRepository affectationRepository,
                       EntretienRepository entretienRepository, VueRepository vueRepository,
                       SiteMapper mapper, Clock clock) {
        this.siteRepository = siteRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.affectationRepository = affectationRepository;
        this.entretienRepository = entretienRepository;
        this.vueRepository = vueRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** F1 : tous les sites triés par ville puis par nom. */

    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Exécute findAll().
     */

    public List<SiteResponse> findAll() {
        return siteRepository.findAllByOrderByVilleAscNomAsc().stream().map(mapper::toResponse).toList();
    }

    /** F1 : un site par identifiant ; 404 si inconnu. */

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Exécute findById().
     */

    public SiteResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F1 : crée un site ; le nom doit être unique. */

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Exécute create().
     */

    @Transactional
    public SiteResponse create(CreateSiteRequest request) {
        if (siteRepository.findByNom(request.nom()).isPresent()) {
            throw new ConflitMetierException("site-nom-deja-utilise",
                    "Le nom de site « " + request.nom() + " » est déjà utilisé");
        }
        return mapper.toResponse(siteRepository.save(
                new Site(request.nom(), request.ville(), request.adresse())));
    }

    /** F1 : adresse et ville modifiables, nom figé. */

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Exécute update().
     */

    @Transactional
    public SiteResponse update(Integer id, UpdateSiteRequest request) {
        Site site = required(id);
        site.modifier(request.adresse(), request.ville());
        return mapper.toResponse(siteRepository.save(site));
    }

    /** F1 : suppression refusée si le site a des véhicules. */

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Exécute delete().
     */

    @Transactional
    public void delete(Integer id) {
        Site site = required(id);
        if (!vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(id).isEmpty()) {
            throw new ConflitMetierException("site-non-supprimable",
                    "Ce site possède encore des véhicules");
        }
        siteRepository.delete(site);
    }

    /** F6 : véhicules d'un site triés par immatriculation avec leur état courant. */

    // ── vehicules() ──────────────────────────────────────────────────────────

    /**
     * Exécute vehicules().
     */

    public List<VehiculeSiteResponse> vehicules(Integer id) {
        required(id);
        LocalDate aujourdHui = LocalDate.now(clock);
        return vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(id).stream()
                .map(vehicule -> new VehiculeSiteResponse(
                        vehicule.getIdVehicule(), vehicule.getImmatriculation(),
                        vehicule.getMarque(), vehicule.getModele(),
                        etatAffectation(vehicule, aujourdHui),
                        vueRepository.bilanVehicule(vehicule.getIdVehicule())
                                .map(VueRepository.BilanRow::kmCompteurActuel).orElse(0)))
                .toList();
    }

    /** F19 : kilométrage total du site sur un mois, détaillé par véhicule. */

    // ── kilometrage() ────────────────────────────────────────────────────────

    /**
     * Exécute kilometrage().
     */

    public KilometrageSiteResponse kilometrage(Integer id, String mois) {
        Site site = required(id);
        YearMonth periode = parseMois(mois);
        List<VueRepository.KilometrageRow> releves = vueRepository.kilometragesParSite(
                id, periode.atDay(1), periode.atEndOfMonth());
        int total = releves.stream().mapToInt(VueRepository.KilometrageRow::km).sum();
        return new KilometrageSiteResponse(
                id, site.getNom(), periode.toString(), total,
                releves.stream()
                        .map(releve -> new DetailKilometrageResponse(
                                releve.idVehicule(), releve.immatriculation(), releve.km()))
                        .toList());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Exécute required().
     */

    private Site required(Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Site", id));
    }

    // ── etatAffectation() ────────────────────────────────────────────────────

    /**
     * Exécute etatAffectation().
     */

    private String etatAffectation(Vehicule vehicule, LocalDate date) {
        Integer id = vehicule.getIdVehicule();
        if (!entretienRepository.findImmobilisationsChevauchant(id, date, date).isEmpty()) {
            return "IMMOBILISE";
        }
        if (!affectationRepository.findChevauchements(id, date, date).isEmpty()) {
            return "AFFECTE";
        }
        return "LIBRE";
    }

    // ── parseMois() ──────────────────────────────────────────────────────────

    /**
     * Exécute parseMois().
     */

    private YearMonth parseMois(String mois) {
        if (mois == null || mois.isBlank()) {
            return YearMonth.from(LocalDate.now(clock));
        }
        try {
            return YearMonth.parse(mois);
        } catch (DateTimeParseException ex) {
            throw new DonneeInvalideException("mois-invalide",
                    "Le mois doit être au format yyyy-MM : " + mois);
        }
    }
}

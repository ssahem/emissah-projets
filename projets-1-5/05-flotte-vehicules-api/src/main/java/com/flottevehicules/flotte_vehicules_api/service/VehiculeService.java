package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.CoutVehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.DisponibleVehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.DisponibilitesParSiteResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.ProchainEntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.RepartitionTypeResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.VehiculeResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.TypeEntretien;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.VehiculeMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.TypeEntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des véhicules (F2), recherche (F9), échéance (F15), coût (F18) et disponibilité
 *   (F17).
 *
 * COUCHE       : Service
 * RÔLE         : CRUD des véhicules (F2), recherche (F9), échéance (F15), coût (F18) et disponibilité (F17).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories métier, VueRepository (vues en lecture seule), Clock,
 *                VehiculeMapper.
 * NE FAIT PAS  : de HTTP ni d'écriture dans les vues.
 */
@Service
@Transactional(readOnly = true)
public class VehiculeService {

    private static final List<String> MODES_ACQUISITION =
            List.of("PROPRIETE", "LOCATION_LONGUE_DUREE", "LEASING");

    private final VehiculeRepository vehiculeRepository;
    private final SiteRepository siteRepository;
    private final AffectationRepository affectationRepository;
    private final EntretienRepository entretienRepository;
    private final TypeEntretienRepository typeEntretienRepository;
    private final VueRepository vueRepository;
    private final VehiculeMapper mapper;
    private final Clock clock;

    public VehiculeService(VehiculeRepository vehiculeRepository, SiteRepository siteRepository,
                           AffectationRepository affectationRepository,
                           EntretienRepository entretienRepository,
                           TypeEntretienRepository typeEntretienRepository,
                           VueRepository vueRepository, VehiculeMapper mapper, Clock clock) {
        this.vehiculeRepository = vehiculeRepository;
        this.siteRepository = siteRepository;
        this.affectationRepository = affectationRepository;
        this.entretienRepository = entretienRepository;
        this.typeEntretienRepository = typeEntretienRepository;
        this.vueRepository = vueRepository;
        this.mapper = mapper;
        this.clock = clock;
    }


    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * F2 et F9 : liste triée, avec recherche optionnelle et combinable.
     */

    public List<VehiculeResponse> findAll(String immatriculation, String marque) {
        List<Vehicule> vehicules = (immatriculation == null && marque == null)
                ? vehiculeRepository.findAllByOrderByImmatriculationAsc()
                : vehiculeRepository.search(immatriculation, marque);
        return vehicules.stream().map(mapper::toResponse).toList();
    }


    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * F2 : un véhicule par identifiant ; 404 si inconnu.
     */

    public VehiculeResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }


    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * F2 : crée un véhicule avec contrôle de la date, du site et du mode
     * d'acquisition.
     */

    @Transactional
    public VehiculeResponse create(CreateVehiculeRequest request) {
        if (vehiculeRepository.findByImmatriculation(request.immatriculation()).isPresent()) {
            throw new ConflitMetierException("vehicule-immatriculation-deja-utilise",
                    "L'immatriculation « " + request.immatriculation() + " » est déjà utilisée");
        }
        if (request.dateMiseEnService().isAfter(LocalDate.now(clock))) {
            throw new DonneeInvalideException("date-mise-en-service-future",
                    "La date de mise en service ne peut pas être dans le futur");
        }
        siteRepository.findById(request.idSite())
                .orElseThrow(() -> new RessourceIntrouvableException("Site", request.idSite()));
        String mode = request.modeAcquisition() == null || request.modeAcquisition().isBlank()
                ? "PROPRIETE" : request.modeAcquisition().trim().toUpperCase();
        if (!MODES_ACQUISITION.contains(mode)) {
            throw new DonneeInvalideException("mode-acquisition-invalide",
                    "Mode d'acquisition accepté : PROPRIETE, LOCATION_LONGUE_DUREE ou LEASING");
        }
        if (request.prixAchat() != null && !"PROPRIETE".equals(mode)) {
            throw new DonneeInvalideException("prix-achat-interdit",
                    "Un prix d'achat n'est autorisé qu'en PROPRIETE");
        }
        if ("PROPRIETE".equals(mode) && request.prixAchat() == null) {
            throw new DonneeInvalideException("prix-achat-obligatoire",
                    "Un prix d'achat est obligatoire en PROPRIETE");
        }
        Vehicule vehicule = vehiculeRepository.save(new Vehicule(
                request.immatriculation(), request.marque(), request.modele(),
                request.dateMiseEnService(), request.idSite(), mode,
                request.partage(), request.prixAchat()));
        return mapper.toResponse(vehicule);
    }


    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * F2 : transfert de site possible ; immatriculation et date de mise en
     * service figées.
     */

    @Transactional
    public VehiculeResponse update(Integer id, UpdateVehiculeRequest request) {
        Vehicule vehicule = required(id);
        siteRepository.findById(request.idSite())
                .orElseThrow(() -> new RessourceIntrouvableException("Site", request.idSite()));
        vehicule.modifier(request.idSite(), request.marque(), request.modele());
        return mapper.toResponse(vehiculeRepository.save(vehicule));
    }


    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * F2 : suppression refusée s'il existe des affectations ou des entretiens.
     */

    @Transactional
    public void delete(Integer id) {
        Vehicule vehicule = required(id);
        if (affectationRepository.existsByIdVehicule(id)) {
            throw new ConflitMetierException("vehicule-non-supprimable",
                    "Ce véhicule possède encore des affectations");
        }
        if (entretienRepository.existsByIdVehicule(id)) {
            throw new ConflitMetierException("vehicule-non-supprimable",
                    "Ce véhicule possède encore des entretiens");
        }
        vehiculeRepository.delete(vehicule);
    }


    // ── prochainEntretien() ──────────────────────────────────────────────────

    /**
     * F15 : première échéance périodique, avec le seuil qui la déclenchera.
     */

    public ProchainEntretienResponse prochainEntretien(Integer id) {
        Vehicule vehicule = required(id);
        List<VueRepository.EcheanceRow> echeances = vueRepository.echeancesVehicule(id);
        if (echeances.isEmpty()) {
            throw new RessourceIntrouvableException("Echéance d'entretien", id);
        }
        VueRepository.EcheanceRow echeance = echeances.get(0);
        int kmRestant = echeance.kmProchain() - echeance.kmActuel();
        long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(clock), echeance.dateProchaine());
        String seuil;
        if (kmRestant <= 0) {
            seuil = "KILOMETRAGE";
        } else if (joursRestants <= 0) {
            seuil = "DUREE";
        } else {
            double fractionKm = (double) kmRestant / Math.max(1, echeance.kmProchain() - echeance.kmDernier());
            double fractionTemps = (double) joursRestants / 365.0;
            seuil = fractionKm <= fractionTemps ? "KILOMETRAGE" : "DUREE";
        }
        return new ProchainEntretienResponse(
                id, vehicule.getImmatriculation(), echeance.libelle(), echeance.dateProchaine(),
                echeance.kmProchain(), echeance.kmActuel(), seuil, echeance.enRetard());
    }


    // ── cout() ───────────────────────────────────────────────────────────────

    /**
     * F18 : coût des entretiens uniquement (D-03) et répartition par type.
     */

    public CoutVehiculeResponse cout(Integer id) {
        Vehicule vehicule = required(id);
        VueRepository.BilanRow bilan = vueRepository.bilanVehicule(id).orElse(
                new VueRepository.BilanRow(id, vehicule.getImmatriculation(),
                        BigDecimal.ZERO, 0, 0, BigDecimal.ZERO));
        BigDecimal coutTotal = vueRepository.coutsVehicule(id)
                .map(VueRepository.CoutRow::coutEntretiens)
                .orElse(bilan.coutTotal());
        Map<Integer, String> libelles = new LinkedHashMap<>();
        for (TypeEntretien type : typeEntretienRepository.findAll()) {
            libelles.put(type.getIdTypeEntretien(), type.getLibelle());
        }
        Map<String, BigDecimal> cumuls = new LinkedHashMap<>();
        for (Entretien entretien : entretienRepository.findAllByIdVehiculeOrderByDateDebutDesc(id)) {
            String libelle = libelles.getOrDefault(entretien.getIdTypeEntretien(), "TYPE INCONNU");
            cumuls.merge(libelle, entretien.getCout(), BigDecimal::add);
        }
        return new CoutVehiculeResponse(
                id, vehicule.getImmatriculation(), coutTotal, bilan.coutAuKm(),
                bilan.kmCompteurActuel(), bilan.ageAnnees(),
                cumuls.entrySet().stream()
                        .map(entry -> new RepartitionTypeResponse(entry.getKey(), entry.getValue()))
                        .toList());
    }


    // ── disponibles() ────────────────────────────────────────────────────────

    /**
     * F17 : véhicules libres sur la période, regroupés par site ; semaine
     * courante par défaut.
     */

    public List<DisponibilitesParSiteResponse> disponibles(LocalDate du, LocalDate au) {
        LocalDate debut = du == null ? LocalDate.now(clock).with(DayOfWeek.MONDAY) : du;
        LocalDate fin = au == null ? debut.plusDays(6) : au;
        if (fin.isBefore(debut)) {
            throw new DonneeInvalideException("periode-invalide",
                    "La date de fin doit être postérieure ou égale à la date de début");
        }
        Map<Integer, List<DisponibleVehiculeResponse>> parSite = new TreeMap<>();
        for (Vehicule vehicule : vehiculeRepository.findAllByOrderByImmatriculationAsc()) {
            Integer id = vehicule.getIdVehicule();
            if (!affectationRepository.findChevauchements(id, debut, fin).isEmpty()
                    || !entretienRepository.findImmobilisationsChevauchant(id, debut, fin).isEmpty()) {
                continue;
            }
            LocalDate prochaineEcheance = vueRepository.echeancesVehicule(id).stream()
                    .map(VueRepository.EcheanceRow::dateProchaine)
                    .filter(date -> date != null)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            DisponibleVehiculeResponse disponible = new DisponibleVehiculeResponse(
                    id, vehicule.getImmatriculation(), mapper.kmActuel(id), prochaineEcheance);
            parSite.computeIfAbsent(vehicule.getIdSite(), site -> new ArrayList<>()).add(disponible);
        }
        List<DisponibilitesParSiteResponse> resultat = new ArrayList<>();
        for (Map.Entry<Integer, List<DisponibleVehiculeResponse>> entree : parSite.entrySet()) {
            Integer idSite = entree.getKey();
            String nomSite = siteRepository.findById(idSite).map(Site::getNom).orElse(null);
            resultat.add(new DisponibilitesParSiteResponse(
                    idSite, nomSite, debut, fin, entree.getValue()));
        }
        return resultat;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Récupère un véhicule par identifiant ou lève une exception si absent.
     */

    private Vehicule required(Integer id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Véhicule", id));
    }
}

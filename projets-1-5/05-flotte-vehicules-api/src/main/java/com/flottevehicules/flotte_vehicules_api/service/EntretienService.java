package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.EntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.HistoriqueEntretienResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.PageResponse;
import com.flottevehicules.flotte_vehicules_api.entity.AlerteVehicule;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.EntretienMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.AlerteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.PrestataireRepository;
import com.flottevehicules.flotte_vehicules_api.repository.TypeEntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des entretiens (F5), historique (F8), pagination (F10) et alerte de conflit (F12).
 *
 * COUCHE       : Service
 * RÔLE         : CRUD des entretiens (F5), historique (F8), pagination (F10) et alerte de conflit (F12).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories métier, VueRepository (compteur et coût), Clock,
 *                EntretienMapper.
 * NE FAIT PAS  : de HTTP ni d'écriture dans les vues.
 */
@Service
@Transactional(readOnly = true)
public class EntretienService {

    private static final String MOTIF_CONFLIT = "CONFLIT_AFFECTATION_ENTRETIEN";

    private final EntretienRepository entretienRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TypeEntretienRepository typeEntretienRepository;
    private final PrestataireRepository prestataireRepository;
    private final AffectationRepository affectationRepository;
    private final AlerteRepository alerteRepository;
    private final VueRepository vueRepository;
    private final EntretienMapper mapper;
    private final Clock clock;

    public EntretienService(EntretienRepository entretienRepository,
                            VehiculeRepository vehiculeRepository,
                            TypeEntretienRepository typeEntretienRepository,
                            PrestataireRepository prestataireRepository,
                            AffectationRepository affectationRepository,
                            AlerteRepository alerteRepository,
                            VueRepository vueRepository, EntretienMapper mapper, Clock clock) {
        this.entretienRepository = entretienRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.typeEntretienRepository = typeEntretienRepository;
        this.prestataireRepository = prestataireRepository;
        this.affectationRepository = affectationRepository;
        this.alerteRepository = alerteRepository;
        this.vueRepository = vueRepository;
        this.mapper = mapper;
        this.clock = clock;
    }


    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * F10 : page d'entretiens, triée par date décroissante par défaut.
     */

    public PageResponse<EntretienResponse> findAll(int page, int size, String sort) {
        if (page < 0) {
            throw new DonneeInvalideException("page-invalide", "L'index de page doit être positif");
        }
        if (size < 1) {
            throw new DonneeInvalideException("taille-invalide", "La taille de page doit être positive");
        }
        int taille = Math.min(size, 100);
        Page<Entretien> resultat = entretienRepository.findAll(
                PageRequest.of(page, taille, tri(sort)));
        return new PageResponse<>(
                resultat.getContent().stream().map(e -> mapper.toResponse(e, false)).toList(),
                resultat.getNumber(), resultat.getSize(),
                resultat.getTotalElements(), resultat.getTotalPages());
    }


    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * F5 : un entretien par identifiant ; 404 si inconnu.
     */

    public EntretienResponse findById(Integer id) {
        return mapper.toResponse(required(id), false);
    }


    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * F5, F14 et F12 : crée un entretien et alerte si une affectation existe
     * déjà.
     */

    @Transactional
    public EntretienResponse create(CreateEntretienRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(request.idVehicule())
                .orElseThrow(() -> new RessourceIntrouvableException("Véhicule", request.idVehicule()));
        typeEntretienRepository.findById(request.idTypeEntretien())
                .orElseThrow(() -> new RessourceIntrouvableException("Type d'entretien", request.idTypeEntretien()));
        prestataireRepository.findById(request.idPrestataire())
                .orElseThrow(() -> new RessourceIntrouvableException("Prestataire", request.idPrestataire()));
        LocalDate date = request.date();
        if (date.isBefore(vehicule.getDateMiseEnService())) {
            throw new DonneeInvalideException("entretien-avant-mise-en-service",
                    "L'entretien ne peut pas précéder la mise en service du véhicule ("
                            + vehicule.getDateMiseEnService() + ")");
        }
        if (request.dateFin() != null && request.dateFin().isBefore(date)) {
            throw new DonneeInvalideException("dates-entretien-inversees",
                    "La date de fin ne peut pas précéder la date de début");
        }
        verifierKmCroissant(request.idVehicule(), request.km());
        Entretien entretien = entretienRepository.save(new Entretien(
                date, request.dateFin(), request.cout(), request.km(), request.idVehicule(),
                request.idTypeEntretien(), request.idPrestataire()));
        boolean alerteCreee = alerterSiConflit(entretien);
        return mapper.toResponse(entretien, alerteCreee);
    }


    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * F5 : coût et prestataire modifiables ; date et kilométrage figés.
     */

    @Transactional
    public EntretienResponse update(Integer id, UpdateEntretienRequest request) {
        Entretien entretien = required(id);
        if (request.date() != null && !request.date().equals(entretien.getDateDebut())) {
            throw new ConflitMetierException("entretien-date-figee",
                    "La date d'un entretien ne peut pas être modifiée");
        }
        if (request.km() != null && !request.km().equals(entretien.getKmCompteur())) {
            throw new ConflitMetierException("entretien-km-fige",
                    "Le kilométrage d'un entretien ne peut pas être modifié");
        }
        prestataireRepository.findById(request.idPrestataire())
                .orElseThrow(() -> new RessourceIntrouvableException("Prestataire", request.idPrestataire()));
        entretien.modifier(request.cout(), request.idPrestataire());
        return mapper.toResponse(entretienRepository.save(entretien), false);
    }


    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * F5 : suppression possible tant que la période d'immobilisation n'a pas
     * commencé.
     */

    @Transactional
    public void delete(Integer id) {
        Entretien entretien = required(id);
        if (!entretien.getDateDebut().isAfter(LocalDate.now(clock))) {
            throw new ConflitMetierException("entretien-non-supprimable",
                    "La période d'immobilisation a déjà commencé");
        }
        entretienRepository.delete(entretien);
    }


    // ── historique() ─────────────────────────────────────────────────────────

    /**
     * F8 : historique d'un véhicule, trié du plus récent au plus ancien, avec
     * cumul D-03.
     */

    public HistoriqueEntretienResponse historique(Integer idVehicule) {
        Vehicule vehicule = vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new RessourceIntrouvableException("Véhicule", idVehicule));
        List<Entretien> entretiens = entretienRepository
                .findAllByIdVehiculeOrderByDateDebutDesc(idVehicule);
        BigDecimal cumul = vueRepository.coutEntretiens(idVehicule);
        if (cumul == null) {
            cumul = BigDecimal.ZERO;
        }
        return new HistoriqueEntretienResponse(
                idVehicule, vehicule.getImmatriculation(), cumul,
                entretiens.stream().map(e -> mapper.toResponse(e, false)).toList());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Récupère un entretien par identifiant ou lève une exception si absent.
     */

    private Entretien required(Integer id) {
        return entretienRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Entretien", id));
    }


    // ── tri() ────────────────────────────────────────────────────────────────

    /**
     * Convertit `date,desc` en tri JPA ; seuls date et cout sont autorisés.
     */

    private Sort tri(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "dateDebut");
        }
        String[] parties = sort.split(",", 2);
        String champ = switch (parties[0].trim().toLowerCase(Locale.ROOT)) {
            case "date" -> "dateDebut";
            case "cout" -> "cout";
            default -> throw new DonneeInvalideException("tri-invalide",
                    "Tri autorisé : date ou cout");
        };
        Sort.Direction direction = parties.length > 1
                && "asc".equalsIgnoreCase(parties[1].trim())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, champ);
    }


    // ── verifierKmCroissant() ────────────────────────────────────────────────

    /**
     * F14 (D-01) : le compteur ne recule pas ; le refus indique la dernière
     * valeur et sa date.
     */

    private void verifierKmCroissant(Integer idVehicule, Integer km) {
        vueRepository.dernierKm(idVehicule).ifPresent(dernier -> {
            if (km < dernier.km()) {
                throw new DonneeInvalideException("kilometrage-decroissant",
                        "Le kilométrage (" + km + ") est inférieur au dernier relevé ("
                                + dernier.km() + ") du " + dernier.date() + ")");
            }
        });
    }


    // ── alerterSiConflit() ───────────────────────────────────────────────────

    /**
     * F12 : un entretien saisi après une affectation ne l'invalide pas, il
     * déclenche une alerte.
     */

    private boolean alerterSiConflit(Entretien entretien) {
        List<Affectation> affectations = affectationRepository.findChevauchements(
                entretien.getIdVehicule(), entretien.getDateDebut(), entretien.finEffective());
        if (affectations.isEmpty()) {
            return false;
        }
        LocalDate echeance = entretien.getDateDebut();
        if (alerteRepository.existsByIdVehiculeAndMotifAndDateEcheance(
                entretien.getIdVehicule(), MOTIF_CONFLIT, echeance)) {
            return false;
        }
        alerteRepository.save(new AlerteVehicule(
                affectations.get(0).getIdAffectation(), entretien.getIdVehicule(),
                MOTIF_CONFLIT, echeance.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                LocalDate.now(clock), echeance, null, null,
                "Entretien " + entretien.getIdEntretien() + " du " + entretien.getDateDebut()
                        + " au " + entretien.finEffective()
                        + " pendant une affectation en cours"));
        return true;
    }
}

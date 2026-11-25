package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.CreateRestitutionRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.AffectationMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SalarieRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des affectations (F4) et règles F11 à F14, F13 inclus.
 *
 * COUCHE       : Service
 * RÔLE         : CRUD des affectations (F4) et règles F11 à F14, F13 inclus.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * ATTENTION    : les contrôles de chevauchement sont applicatifs ; une concurrence simultanée
 *                sur le même véhicule peut nécessiter un verrou SQL (décision S-07).
 * NE FAIT PAS  : de HTTP ni d'écriture directe dans les vues.
 */
@Service
@Transactional(readOnly = true)
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final SalarieRepository salarieRepository;
    private final EntretienRepository entretienRepository;
    private final VueRepository vueRepository;
    private final AffectationMapper mapper;
    private final Clock clock;

    public AffectationService(AffectationRepository affectationRepository,
                              VehiculeRepository vehiculeRepository,
                              SalarieRepository salarieRepository,
                              EntretienRepository entretienRepository,
                              VueRepository vueRepository,
                              AffectationMapper mapper, Clock clock) {
        this.affectationRepository = affectationRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.salarieRepository = salarieRepository;
        this.entretienRepository = entretienRepository;
        this.vueRepository = vueRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** F4 : affectations triées par date de début décroissante. */

    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Exécute findAll().
     */

    public List<AffectationResponse> findAll() {
        return affectationRepository.findAllByOrderByDateDebutDesc().stream()
                .map(mapper::toResponse).toList();
    }

    /** F4 : une affectation par identifiant ; 404 si inconnue. */

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Exécute findById().
     */

    public AffectationResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F4, F11, F12 : crée une affectation après contrôle des chevauchements. */

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Exécute create().
     */

    @Transactional
    public AffectationResponse create(CreateAffectationRequest request) {
        LocalDate aujourdHui = LocalDate.now(clock);
        Vehicule vehicule = vehiculeRepository.findById(request.idVehicule())
                .orElseThrow(() -> new RessourceIntrouvableException("Véhicule", request.idVehicule()));
        salarieRepository.findById(request.idSalarie())
                .orElseThrow(() -> new RessourceIntrouvableException("Salarié", request.idSalarie()));
        LocalDate debut = request.dateDebut();
        LocalDate fin = request.dateFin();
        if (fin != null && fin.isBefore(debut)) {
            throw new DonneeInvalideException("dates-affectation-inversees",
                    "La date de fin ne peut pas précéder la date de début");
        }
        if (debut.isBefore(vehicule.getDateMiseEnService())) {
            throw new DonneeInvalideException("affectation-avant-mise-en-service",
                    "L'affectation ne peut pas commencer avant la mise en service du véhicule ("
                            + vehicule.getDateMiseEnService() + ")");
        }
        verifierDisponibilite(request.idVehicule(), debut, fin, null);
        Affectation affectation = affectationRepository.save(
                new Affectation(debut, fin, request.idVehicule(), request.idSalarie()));
        return mapper.toResponse(affectation);
    }

    /** F4 : modification de la date de fin ; parties figées dès le début de l'affectation. */

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Exécute update().
     */

    @Transactional
    public AffectationResponse update(Integer id, UpdateAffectationRequest request) {
        Affectation affectation = required(id);
        LocalDate aujourdHui = LocalDate.now(clock);
        boolean commencee = !affectation.getDateDebut().isAfter(aujourdHui);
        if (request.idVehicule() != null && !request.idVehicule().equals(affectation.getIdVehicule())) {
            if (commencee) {
                throw new ConflitMetierException("affectation-vehicule-fige",
                        "Le véhicule est figé dès le début de l'affectation");
            }
            verifierDisponibilite(request.idVehicule(), affectation.getDateDebut(),
                    request.dateFin() == null ? affectation.getDateFin() : request.dateFin(), null);
        }
        if (request.idSalarie() != null && !request.idSalarie().equals(affectation.getIdSalarie())) {
            if (commencee) {
                throw new ConflitMetierException("affectation-salarie-fige",
                        "Le salarié est figé dès le début de l'affectation");
            }
            salarieRepository.findById(request.idSalarie())
                    .orElseThrow(() -> new RessourceIntrouvableException("Salarié", request.idSalarie()));
        }
        LocalDate nouvelleFin = request.dateFin() == null ? affectation.getDateFin() : request.dateFin();
        if (nouvelleFin != null && nouvelleFin.isBefore(affectation.getDateDebut())) {
            throw new DonneeInvalideException("dates-affectation-inversees",
                    "La date de fin ne peut pas précéder la date de début");
        }
        if (!commencee) {
            verifierDisponibilite(affectation.getIdVehicule(), affectation.getDateDebut(),
                    nouvelleFin, id);
        }
        affectation.modifierDateFin(nouvelleFin);
        if (!commencee) {
            affectation.modifierParties(
                    request.idVehicule() == null ? affectation.getIdVehicule() : request.idVehicule(),
                    request.idSalarie() == null ? affectation.getIdSalarie() : request.idSalarie());
        }
        return mapper.toResponse(affectationRepository.save(affectation));
    }

    /** F4 : suppression possible uniquement avant le début de l'affectation. */

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Exécute delete().
     */

    @Transactional
    public void delete(Integer id) {
        Affectation affectation = required(id);
        if (!affectation.getDateDebut().isAfter(LocalDate.now(clock))) {
            throw new ConflitMetierException("affectation-non-supprimable",
                    "Une affectation commencée se clôture par une restitution");
        }
        affectationRepository.delete(affectation);
    }

    /** F13 et F14 : clôture l'affectation avec date et relevé kilométrique croissant. */

    // ── restituer() ──────────────────────────────────────────────────────────

    /**
     * Exécute restituer().
     */

    @Transactional
    public AffectationResponse restituer(Integer id, CreateRestitutionRequest request) {
        Affectation affectation = required(id);
        if (affectation.getDateFin() != null || affectation.getKmRestitution() != null) {
            throw new ConflitMetierException("affectation-deja-cloturee",
                    "Cette affectation est déjà clôturée");
        }
        LocalDate date = request.date();
        LocalDate aujourdHui = LocalDate.now(clock);
        if (!date.isAfter(affectation.getDateDebut())) {
            throw new DonneeInvalideException("restitution-date-invalide",
                    "La date de restitution doit être postérieure au début de l'affectation ("
                            + affectation.getDateDebut() + ")");
        }
        if (date.isAfter(aujourdHui)) {
            throw new DonneeInvalideException("restitution-date-future",
                    "La date de restitution ne peut pas être dans le futur");
        }
        verifierKmCroissant(affectation.getIdVehicule(), request.km());
        affectation.clore(date, request.km());
        return mapper.toResponse(affectationRepository.save(affectation));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Exécute required().
     */

    private Affectation required(Integer id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Affectation", id));
    }

    /** F11 et F12 : refuse le chevauchement avec une affectation ou une immobilisation. */

    // ── verifierDisponibilite() ──────────────────────────────────────────────

    /**
     * Exécute verifierDisponibilite().
     */

    private void verifierDisponibilite(Integer idVehicule, LocalDate debut, LocalDate fin, Integer idExclu) {
        // LocalDate.MAX déborde lors de la conversion JDBC ; 9999 reste dans l'intervalle PostgreSQL.
        LocalDate finEffective = fin == null ? LocalDate.of(9999, 12, 31) : fin;
        List<Affectation> bloquantes = affectationRepository
                .findChevauchements(idVehicule, debut, finEffective)
                .stream().filter(a -> idExclu == null || !a.getIdAffectation().equals(idExclu)).toList();
        if (!bloquantes.isEmpty()) {
            Affectation bloquante = bloquantes.get(0);
            Salarie salarie = salarieRepository.findById(bloquante.getIdSalarie()).orElse(null);
            String who = salarie == null ? "salarié " + bloquante.getIdSalarie()
                    : salarie.getPrenom() + " " + salarie.getNom();
            throw new ConflitMetierException("affectation-chevauchement",
                    "Le véhicule est déjà affecté à " + who + " sur cette période (affectation "
                            + bloquante.getIdAffectation() + ")")
                    .avec("affectationBloquante", bloquante.getIdAffectation())
                    .avec("salarié", who);
        }
        List<Entretien> immobilisations = entretienRepository
                .findImmobilisationsChevauchant(idVehicule, debut, finEffective);
        if (!immobilisations.isEmpty()) {
            Entretien entretien = immobilisations.get(0);
            throw new ConflitMetierException("affectation-immobilisee",
                    "Le véhicule est immobilisé par l'entretien " + entretien.getIdEntretien()
                            + " du " + entretien.getDateDebut() + " au "
                            + entretien.finEffective())
                    .avec("entretien", entretien.getIdEntretien());
        }
    }

    /** F14 (D-01) : le compteur ne recule pas ; le refus indique la dernière valeur et sa date. */

    // ── verifierKmCroissant() ────────────────────────────────────────────────

    /**
     * Exécute verifierKmCroissant().
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
}

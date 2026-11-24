package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateObtentionCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.PlanningInterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.TechnicienMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD des techniciens (F2), compétences (F7) et planning (F10). */
@Service
@Transactional(readOnly = true)
public class TechnicienService {

    private final TechnicienRepository technicienRepository;
    private final EquipeRepository equipeRepository;
    private final AgenceRepository agenceRepository;
    private final AffectationRepository affectationRepository;
    private final ObtentionCompetenceRepository obtentionRepository;
    private final CompetenceRepository competenceRepository;
    private final InterventionRepository interventionRepository;
    private final AdresseRepository adresseRepository;
    private final ClientRepository clientRepository;
    private final HistoriqueStatutRepository historiqueRepository;
    private final StatutRepository statutRepository;
    private final TechnicienMapper mapper;
    private final Clock clock;

    public TechnicienService(TechnicienRepository technicienRepository,
                             EquipeRepository equipeRepository, AgenceRepository agenceRepository,
                             AffectationRepository affectationRepository,
                             ObtentionCompetenceRepository obtentionRepository,
                             CompetenceRepository competenceRepository,
                             InterventionRepository interventionRepository,
                             AdresseRepository adresseRepository, ClientRepository clientRepository,
                             HistoriqueStatutRepository historiqueRepository,
                             StatutRepository statutRepository, TechnicienMapper mapper, Clock clock) {
        this.technicienRepository = technicienRepository;
        this.equipeRepository = equipeRepository;
        this.agenceRepository = agenceRepository;
        this.affectationRepository = affectationRepository;
        this.obtentionRepository = obtentionRepository;
        this.competenceRepository = competenceRepository;
        this.interventionRepository = interventionRepository;
        this.adresseRepository = adresseRepository;
        this.clientRepository = clientRepository;
        this.historiqueRepository = historiqueRepository;
        this.statutRepository = statutRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<TechnicienResponse> findAll() {
        return technicienRepository.findAllByOrderByNomAsc().stream().map(mapper::toResponse).toList();
    }

    public TechnicienResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F7 : toutes les compétences obtenues, expirées comprises mais signalées comme telles. */
    public List<CompetenceTechnicienResponse> findCompetences(Integer id) {
        required(id);
        LocalDate aujourdHui = LocalDate.now(clock);
        return obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(id).stream()
                .map(o -> {
                    Competence competence = competenceRepository.findById(o.getIdCompetence())
                            .orElseThrow(() -> new RessourceIntrouvableException(
                                    "Compétence", o.getIdCompetence()));
                    boolean expiree = o.getDateExpiration() != null
                            && o.getDateExpiration().isBefore(aujourdHui);
                    return new CompetenceTechnicienResponse(competence.getIdCompetence(),
                            competence.getLibelle(), o.getDateObtention(), o.getDateExpiration(), expiree);
                })
                .toList();
    }

    /** F10 : planning sur une période, semaine courante par défaut, 3 mois maximum. */
    public List<PlanningInterventionResponse> findPlanning(Integer id, LocalDate du, LocalDate au) {
        required(id);
        LocalDate debut = du != null ? du : LocalDate.now(clock).with(DayOfWeek.MONDAY);
        LocalDate fin = au != null ? au : debut.plusDays(7);
        if (fin.isAfter(debut.plusMonths(3))) {
            throw new DonneeInvalideException("periode-trop-longue",
                    "La période demandée ne peut pas dépasser trois mois");
        }
        if (fin.isBefore(debut)) {
            throw new DonneeInvalideException("periode-invalide",
                    "La date de fin ne peut pas être antérieure à la date de début");
        }
        LocalDateTime debutPeriode = debut.atStartOfDay();
        LocalDateTime finPeriode = fin.plusDays(1).atStartOfDay();

        List<Integer> ids = affectationRepository.findAllByIdTechnicien(id).stream()
                .map(Affectation::getIdIntervention).distinct().toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return interventionRepository.findAllByIdInterventionInOrderByDebutPrevuAsc(ids).stream()
                .filter(i -> i.getDebutPrevu().isBefore(finPeriode)
                        && i.getFinPrevue().isAfter(debutPeriode))
                .map(i -> {
                    Client client = client(i);
                    Adresse adresse = adresse(i);
                    return new PlanningInterventionResponse(
                            i.getIdIntervention(), i.getDebutPrevu(), i.getFinPrevue(),
                            client.getNom(), client.getPrenom(),
                            adresse.getVoie(), adresse.getCommune(),
                            statutCourant(i) == StatutIntervention.ANNULEE);
                })
                .toList();
    }

    /** F11 : certifier une compétence, en refusant les dates incohérentes et les doublons actifs. */
    @Transactional
    public CompetenceTechnicienResponse associerCompetence(
            Integer idTechnicien, CreateObtentionCompetenceRequest request) {
        required(idTechnicien);
        Competence competence = competenceRepository.findById(request.idCompetence())
                .orElseThrow(() -> new RessourceIntrouvableException("Compétence", request.idCompetence()));
        if (request.dateExpiration() != null
                && !request.dateExpiration().isAfter(request.dateObtention())) {
            throw new DonneeInvalideException("dates-competence-incoherentes",
                    "La date d'expiration doit être postérieure à la date d'obtention");
        }
        LocalDate aujourdHui = LocalDate.now(clock);
        if (obtentionRepository.existsCompetenceActive(idTechnicien, request.idCompetence(), aujourdHui)) {
            throw new ConflitMetierException("competence-active-deja-obtenue",
                    "Ce technicien possède déjà cette compétence active");
        }
        ObtentionCompetence obtention = obtentionRepository.save(new ObtentionCompetence(
                request.dateObtention(), request.dateExpiration(), idTechnicien, request.idCompetence()));
        return new CompetenceTechnicienResponse(competence.getIdCompetence(), competence.getLibelle(),
                obtention.getDateObtention(), obtention.getDateExpiration(),
                obtention.getDateExpiration() != null && obtention.getDateExpiration().isBefore(aujourdHui));
    }

    @Transactional
    public TechnicienResponse create(CreateTechnicienRequest request) {
        if (technicienRepository.existsByMatricule(request.matricule())) {
            throw new ConflitMetierException("technicien-matricule-deja-utilise",
                    "Le matricule « " + request.matricule() + " » est déjà utilisé");
        }
        Integer idEquipe = resolveEquipe(request.idAgence(), request.idEquipe());
        String email = request.email() != null && !request.email().isBlank()
                ? request.email() : "technicien-" + request.matricule() + "@example.test";
        if (technicienRepository.existsByEmail(email)) {
            throw new ConflitMetierException("technicien-email-deja-utilise",
                    "Cet email est déjà utilisé");
        }
        String prenom = request.prenom() != null ? request.prenom() : "";
        Technicien technicien = technicienRepository.save(new Technicien(
                request.nom(), prenom, email, request.telephone(), idEquipe, request.matricule()));
        return mapper.toResponse(technicien);
    }

    @Transactional
    public TechnicienResponse update(Integer id, UpdateTechnicienRequest request) {
        Technicien technicien = required(id);
        if (!technicien.getMatricule().equals(request.matricule())) {
            throw new ConflitMetierException("technicien-matricule-fige",
                    "Le matricule d'un technicien ne peut pas être modifié");
        }
        Integer idEquipe = resolveEquipe(request.idAgence(), request.idEquipe());
        technicien.modifier(request.nom(),
                request.prenom() != null ? request.prenom() : "",
                request.email(), request.telephone(), idEquipe);
        return mapper.toResponse(technicien);
    }

    @Transactional
    public void delete(Integer id) {
        Technicien technicien = required(id);
        if (affectationRepository.existsByIdTechnicien(id)) {
            throw new ConflitMetierException("technicien-non-supprimable",
                    "Ce technicien est encore affecté à une intervention");
        }
        technicienRepository.delete(technicien);
    }

    private Client client(Intervention intervention) {
        return clientRepository.findById(intervention.getIdClient())
                .orElseThrow(() -> new RessourceIntrouvableException("Client", intervention.getIdClient()));
    }

    private Adresse adresse(Intervention intervention) {
        return adresseRepository.findById(intervention.getIdAdresse())
                .orElseThrow(() -> new RessourceIntrouvableException("Adresse", intervention.getIdAdresse()));
    }

    private StatutIntervention statutCourant(Intervention intervention) {
        return affectationRepository.findByIdInterventionAndEstResponsableTrue(intervention.getIdIntervention())
                .flatMap(a -> historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(
                        a.getIdAffectation()))
                .flatMap(h -> statutRepository.findById(h.getIdStatut()))
                .map(s -> StatutIntervention.fromLibelle(s.getLibelle()))
                .filter(Objects::nonNull)
                .orElse(StatutIntervention.PLANIFIEE);
    }

    private Integer resolveEquipe(Integer idAgence, Integer idEquipe) {
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new RessourceIntrouvableException("Agence", idAgence));
        if (idEquipe != null) {
            if (!equipeRepository.existsByIdEquipeAndIdAgence(idEquipe, idAgence)) {
                throw new DonneeInvalideException("equipe-hors-agence",
                        "L'équipe " + idEquipe + " n'appartient pas à l'agence " + idAgence);
            }
            return idEquipe;
        }
        return equipeRepository.findFirstByIdAgenceOrderByIdEquipeAsc(idAgence)
                .orElseGet(() -> equipeRepository.save(new Equipe("Équipe " + agence.getNom(), idAgence)))
                .getIdEquipe();
    }

    private Technicien required(Integer id) {
        return technicienRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Technicien", id));
    }
}

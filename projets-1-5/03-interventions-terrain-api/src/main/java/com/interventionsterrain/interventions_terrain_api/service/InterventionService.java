package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.request.AddAffectationRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateRepriseRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateStatutRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AffectationResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.AffectationMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.InterventionMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TypeInterventionRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des interventions (F5), avec responsable unique et statut initial (A-02, A-03).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles métier décrites dans l'en-tête, piloter les transactions
 *                et orchestrer repositories et mappers.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : AffectationMapper, InterventionMapper, AdresseRepository,
 *                AffectationRepository, ClientRepository, CompetenceRepository,
 *                ExigenceCompetenceRepository, HistoriqueStatutRepository,
 *                InterventionRepository, ObtentionCompetenceRepository, StatutRepository,
 *                TechnicienRepository, TypeInterventionRepository
 * UTILISÉ PAR  : InterventionController.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class InterventionService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Accès en base à la table associée à intervention. */
    private final InterventionRepository interventionRepository;
    /** Accès en base à la table associée à adresse. */
    private final AdresseRepository adresseRepository;
    /** Accès en base à la table associée à client. */
    private final ClientRepository clientRepository;
    /** Accès en base à la table associée à technicien. */
    private final TechnicienRepository technicienRepository;
    /** Accès en base à la table associée à type. */
    private final TypeInterventionRepository typeRepository;
    /** Accès en base à la table associée à statut. */
    private final StatutRepository statutRepository;
    /** Accès en base à la table associée à affectation. */
    private final AffectationRepository affectationRepository;
    /** Accès en base à la table associée à historique. */
    private final HistoriqueStatutRepository historiqueRepository;
    /** Accès en base à la table associée à exigence. */
    private final ExigenceCompetenceRepository exigenceRepository;
    /** Accès en base à la table associée à obtention. */
    private final ObtentionCompetenceRepository obtentionRepository;
    /** Accès en base à la table associée à competence. */
    private final CompetenceRepository competenceRepository;
    /** Conversion entité ↔ DTO, sans logique métier. */
    private final InterventionMapper mapper;
    private final AffectationMapper affectationMapper;
    /** Horloge applicative injectée : toute date produite par le service passe par elle. */
    private final Clock clock;

    /** Transitions autorisées du cycle de vie (F14) ; EN_ROUTE et REPORTEE restent hors écriture. */
    private static final Map<StatutIntervention, Set<StatutIntervention>> TRANSITIONS = Map.of(
            StatutIntervention.PLANIFIEE, Set.of(StatutIntervention.EN_COURS, StatutIntervention.ANNULEE),
            StatutIntervention.EN_COURS, Set.of(StatutIntervention.TERMINEE, StatutIntervention.ECHOUEE));

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    public InterventionService(InterventionRepository interventionRepository,
                               AdresseRepository adresseRepository, ClientRepository clientRepository,
                               TechnicienRepository technicienRepository,
                               TypeInterventionRepository typeRepository, StatutRepository statutRepository,
                               AffectationRepository affectationRepository,
                               HistoriqueStatutRepository historiqueRepository,
                               ExigenceCompetenceRepository exigenceRepository,
                               ObtentionCompetenceRepository obtentionRepository,
                               CompetenceRepository competenceRepository,
                               InterventionMapper mapper, AffectationMapper affectationMapper,
                               Clock clock) {
        this.interventionRepository = interventionRepository;
        this.adresseRepository = adresseRepository;
        this.clientRepository = clientRepository;
        this.technicienRepository = technicienRepository;
        this.typeRepository = typeRepository;
        this.statutRepository = statutRepository;
        this.affectationRepository = affectationRepository;
        this.historiqueRepository = historiqueRepository;
        this.exigenceRepository = exigenceRepository;
        this.obtentionRepository = obtentionRepository;
        this.competenceRepository = competenceRepository;
        this.mapper = mapper;
        this.affectationMapper = affectationMapper;
        this.clock = clock;
    }

    /** F9 : filtres optionnels et combinables sur le statut du responsable et le début prévu. */
    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Exécute findAll().
     */

    public List<InterventionResponse> findAll(String statut, LocalDate du, LocalDate au) {
        String libelle = null;
        if (statut != null && !statut.isBlank()) {
            if (StatutIntervention.fromLibelle(statut) == null) {
                throw new DonneeInvalideException("statut-inconnu",
                        "Le statut « " + statut + " » est inconnu");
            }
            libelle = statut;
        }
        if (du != null && au != null && du.isAfter(au)) {
            throw new DonneeInvalideException("periode-invalide",
                    "La date de début de période ne peut pas être postérieure à la date de fin");
        }
        LocalDateTime debut = du != null ? du.atStartOfDay() : null;
        LocalDateTime fin = au != null ? au.plusDays(1).atStartOfDay() : null;
        return interventionRepository.findAllFiltres(libelle, debut, fin).stream()
                .map(this::toResponse).toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Exécute findById().
     */

    public InterventionResponse findById(Integer id) {
        return toResponse(required(id));
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Exécute create().
     */

    @Transactional
    public InterventionResponse create(CreateInterventionRequest request) {
        if (!clientRepository.existsById(request.idClient())) {
            throw new RessourceIntrouvableException("Client", request.idClient());
        }
        TypeIntervention type = typeRepository.findByCode(request.codeType())
                .orElseThrow(() -> new DonneeInvalideException("type-intervention-inconnu",
                        "Le code de type d'intervention « " + request.codeType() + " » est inconnu"));
        requiredTechnicien(request.idTechnicienResponsable());
        verifierCreneau(request.debutPrevu(), request.finPrevue());
        verifierDisponibilite(request.idTechnicienResponsable(), request.debutPrevu(),
                request.finPrevue(), null);
        verifierCompetences(request.idTechnicienResponsable(), type.getIdType(), request.debutPrevu());

        Adresse adresse = adresseRepository.save(new Adresse(request.voie(), request.commune()));
        Intervention intervention = interventionRepository.save(new Intervention(
                request.dateDemande(), request.debutPrevu(), request.finPrevue(),
                request.idClient(), adresse.getIdAdresse(), type.getCode()));
        Affectation responsable = affectationRepository.save(
                new Affectation(intervention.getIdIntervention(), request.idTechnicienResponsable(), true));
        addHistorique(responsable, StatutIntervention.PLANIFIEE);
        return toResponse(intervention);
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Exécute update().
     */

    @Transactional
    public InterventionResponse update(Integer id, UpdateInterventionRequest request) {
        Intervention intervention = required(id);
        verifierCreneau(request.debutPrevu(), request.finPrevue());
        requiredTechnicien(request.idTechnicienResponsable());
        Affectation responsable = affectationRepository
                .findByIdInterventionAndEstResponsableTrue(id)
                .orElseThrow(() -> new ConflitMetierException("responsable-absent",
                        "Cette intervention n'a pas de technicien responsable"));
        verifierModifiable(responsable);
        verifierDisponibilite(request.idTechnicienResponsable(), request.debutPrevu(),
                request.finPrevue(), id);
        verifierCompetences(request.idTechnicienResponsable(), typeDe(intervention).getIdType(),
                request.debutPrevu());
        intervention.modifierCreneau(request.debutPrevu(), request.finPrevue());
        if (!responsable.getIdTechnicien().equals(request.idTechnicienResponsable())) {
            StatutIntervention courant = statutCourant(responsable);
            responsable.setEstResponsable(false);
            // L'index partiel n'autorise qu'un responsable : libérer l'ancien avant d'élire le nouveau.
            affectationRepository.flush();
            Affectation nouveau = affectationRepository.findByIdInterventionAndIdTechnicien(
                            id, request.idTechnicienResponsable())
                    .orElseGet(() -> affectationRepository.save(
                            new Affectation(id, request.idTechnicienResponsable(), false)));
            nouveau.setEstResponsable(true);
            addHistorique(nouveau, courant);
        }
        return toResponse(intervention);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Exécute delete().
     */

    @Transactional
    public void delete(Integer id) {
        Intervention intervention = required(id);
        affectationRepository.findByIdInterventionAndEstResponsableTrue(id)
                .ifPresent(this::verifierModifiable);
        for (Affectation affectation : affectationRepository.findAllByIdIntervention(id)) {
            historiqueRepository.deleteByIdAffectation(affectation.getIdAffectation());
        }
        affectationRepository.deleteByIdIntervention(id);
        interventionRepository.delete(intervention);
    }

    // ── addAffectation() ─────────────────────────────────────────────────────

    /**
     * Exécute addAffectation().
     */

    @Transactional
    public AffectationResponse addAffectation(Integer id, AddAffectationRequest request) {
        required(id);
        requiredTechnicien(request.idTechnicien());
        if (affectationRepository.findByIdInterventionAndIdTechnicien(id, request.idTechnicien()).isPresent()) {
            throw new ConflitMetierException("technicien-deja-affecte",
                    "Ce technicien est déjà affecté à cette intervention");
        }
        boolean responsable = request.responsable();
        if (responsable) {
            if (affectationRepository.findByIdInterventionAndEstResponsableTrue(id).isPresent()) {
                throw new ConflitMetierException("second-responsable",
                        "Cette intervention a déjà un technicien responsable");
            }
        } else {
            affectationRepository.findByIdInterventionAndEstResponsableTrue(id)
                    .ifPresent(this::verifierModifiable);
        }
        Affectation affectation = affectationRepository.save(
                new Affectation(id, request.idTechnicien(), responsable));
        if (responsable) {
            addHistorique(affectation, StatutIntervention.PLANIFIEE);
        }
        return affectationMapper.toResponse(affectation);
    }

    // ── removeAffectation() ──────────────────────────────────────────────────

    /**
     * Exécute removeAffectation().
     */

    @Transactional
    public void removeAffectation(Integer id, Integer idTechnicien) {
        required(id);
        Affectation affectation = affectationRepository.findByIdInterventionAndIdTechnicien(id, idTechnicien)
                .orElseThrow(() -> new RessourceIntrouvableException("Affectation", idTechnicien));
        if (affectation.isEstResponsable()) {
            throw new ConflitMetierException("responsable-non-retirable",
                    "Le technicien responsable se remplace via la modification de l'intervention");
        }
        historiqueRepository.deleteByIdAffectation(affectation.getIdAffectation());
        affectationRepository.delete(affectation);
    }

    // ── findAffectations() ───────────────────────────────────────────────────

    /**
     * Exécute findAffectations().
     */

    public List<AffectationResponse> findAffectations(Integer id) {
        required(id);
        return affectationRepository.findAllByIdIntervention(id).stream()
                .map(affectationMapper::toResponse).toList();
    }

    /** F14 : transition du statut courant, horodatée sur l'affectation responsable. */

    // ── changerStatut() ──────────────────────────────────────────────────────

    /**
     * Exécute changerStatut().
     */

    @Transactional
    public InterventionResponse changerStatut(Integer id, UpdateStatutRequest request) {
        Intervention intervention = required(id);
        Affectation responsable = requiredResponsable(id);
        StatutIntervention cible = StatutIntervention.fromLibelle(request.statut());
        if (cible == null) {
            throw new DonneeInvalideException("statut-inconnu",
                    "Le statut « " + request.statut() + " » est inconnu");
        }
        StatutIntervention courant = statutCourant(responsable);
        Set<StatutIntervention> atteignables = TRANSITIONS.getOrDefault(courant, Set.of());
        if (!atteignables.contains(cible)) {
            List<String> libelles = atteignables.stream().map(Enum::name).sorted().toList();
            throw new ConflitMetierException("transition-interdite",
                    "Transition de " + courant + " vers " + cible + " refusée",
                    Map.of("statutCourant", courant.name(), "atteignables", libelles));
        }
        addHistorique(responsable, cible);
        return toResponse(intervention);
    }

    /** F15 : reprogramme une intervention ECHOUEE non encore reprise. */

    // ── creerReprise() ───────────────────────────────────────────────────────

    /**
     * Exécute creerReprise().
     */

    @Transactional
    public InterventionResponse creerReprise(Integer id, CreateRepriseRequest request) {
        Intervention origine = required(id);
        Affectation responsableOrigine = requiredResponsable(id);
        if (statutCourant(responsableOrigine) != StatutIntervention.ECHOUEE) {
            throw new ConflitMetierException("reprise-statut-invalide",
                    "Seule une intervention ECHOUEE peut être reprise");
        }
        if (interventionRepository.existsByIdInterventionOrigine(id)) {
            throw new ConflitMetierException("reprise-deja-existante",
                    "Cette intervention a déjà été reprise");
        }
        verifierCreneau(request.debutPrevu(), request.finPrevue());
        verifierDisponibilite(responsableOrigine.getIdTechnicien(), request.debutPrevu(),
                request.finPrevue(), id);
        verifierCompetences(responsableOrigine.getIdTechnicien(), typeDe(origine).getIdType(),
                request.debutPrevu());

        Intervention reprise = interventionRepository.save(new Intervention(
                LocalDateTime.now(clock), request.debutPrevu(), request.finPrevue(),
                origine.getIdClient(), origine.getIdAdresse(), origine.getCodeTypeIntervention()));
        reprise.modifierOrigine(id);
        Affectation responsable = affectationRepository.save(new Affectation(
                reprise.getIdIntervention(), responsableOrigine.getIdTechnicien(), true));
        addHistorique(responsable, StatutIntervention.PLANIFIEE);
        return toResponse(reprise);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    private InterventionResponse toResponse(Intervention intervention) {
        Adresse adresse = adresseRepository.findById(intervention.getIdAdresse()).orElse(null);
        Affectation responsable = affectationRepository
                .findByIdInterventionAndEstResponsableTrue(intervention.getIdIntervention())
                .orElse(null);
        return mapper.toResponse(intervention, adresse, responsable);
    }

    // ── addHistorique() ──────────────────────────────────────────────────────

    /**
     * Exécute addHistorique().
     */

    private void addHistorique(Affectation affectation, StatutIntervention statut) {
        Statut ligne = statutRepository.findByLibelle(statut.name())
                .orElseThrow(() -> new RessourceIntrouvableException("Statut", statut.name()));
        historiqueRepository.save(new HistoriqueStatut(
                LocalDateTime.now(clock), ligne.getIdStatut(), affectation.getIdAffectation()));
    }

    // ── statutCourant() ──────────────────────────────────────────────────────

    /**
     * Exécute statutCourant().
     */

    private StatutIntervention statutCourant(Affectation affectation) {
        if (affectation == null) {
            return StatutIntervention.PLANIFIEE;
        }
        return historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(
                        affectation.getIdAffectation())
                .flatMap(h -> statutRepository.findById(h.getIdStatut()))
                .map(s -> StatutIntervention.fromLibelle(s.getLibelle()))
                .orElse(StatutIntervention.PLANIFIEE);
    }

    /** Vérifie l'antériorité du créneau et son caractère futur (F5). */

    // ── verifierCreneau() ────────────────────────────────────────────────────

    /**
     * Exécute verifierCreneau().
     */

    private void verifierCreneau(LocalDateTime debut, LocalDateTime fin) {
        if (!fin.isAfter(debut)) {
            throw new DonneeInvalideException("creneau-invalide",
                    "La fin prévue doit être postérieure au début prévu");
        }
        if (!debut.isAfter(LocalDateTime.now(clock))) {
            throw new DonneeInvalideException("creneau-passe",
                    "Le début du créneau doit être dans le futur");
        }
    }

    /** Une intervention n'est modifiable ou supprimable que tant qu'elle est PLANIFIEE (F5). */

    // ── verifierModifiable() ─────────────────────────────────────────────────

    /**
     * Exécute verifierModifiable().
     */

    private void verifierModifiable(Affectation responsable) {
        if (statutCourant(responsable) != StatutIntervention.PLANIFIEE) {
            throw new ConflitMetierException("intervention-non-modifiable",
                    "Seule une intervention PLANIFIEE peut être modifiée ou supprimée");
        }
    }

    /** F12 : verrou pessimiste sur le technicien, puis recherche des chevauchements actifs. */

    // ── verifierDisponibilite() ──────────────────────────────────────────────

    /**
     * Exécute verifierDisponibilite().
     */

    private void verifierDisponibilite(Integer idTechnicien, LocalDateTime debut, LocalDateTime fin,
                                       Integer idInterventionExclu) {
        // Le verrou est pris même si le résultat n'est pas utilisé : il sérialise ce contrôle.
        technicienRepository.findByIdForUpdate(idTechnicien);
        List<Intervention> chevauchements = interventionRepository.findChevauchements(
                idTechnicien, debut, fin, idInterventionExclu != null ? idInterventionExclu : -1);
        for (Intervention chevauchement : chevauchements) {
            Affectation responsable = affectationRepository
                    .findByIdInterventionAndEstResponsableTrue(chevauchement.getIdIntervention())
                    .orElse(null);
            if (statutCourant(responsable) == StatutIntervention.ANNULEE) {
                continue;
            }
            throw new ConflitMetierException("chevauchement-creneau",
                    "Le technicien est déjà occupé sur ce créneau",
                    Map.of("idInterventionBloquant", chevauchement.getIdIntervention(),
                            "debut", chevauchement.getDebutPrevu(),
                            "fin", chevauchement.getFinPrevue()));
        }
    }

    /** F13 : toutes les compétences exigées doivent être détenues et valides à la date prévue. */

    // ── verifierCompetences() ────────────────────────────────────────────────

    /**
     * Exécute verifierCompetences().
     */

    private void verifierCompetences(Integer idTechnicien, Integer idType, LocalDateTime debutPrevu) {
        List<ExigenceCompetence> exigences = exigenceRepository.findAllByIdIdType(idType);
        if (exigences.isEmpty()) {
            return;
        }
        List<ObtentionCompetence> obtentions =
                obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(idTechnicien);
        LocalDate datePrevue = debutPrevu.toLocalDate();
        List<String> manquantes = new ArrayList<>();
        for (ExigenceCompetence exigence : exigences) {
            Integer idCompetence = exigence.getId().getIdCompetence();
            boolean satisfaite = obtentions.stream()
                    .filter(o -> o.getIdCompetence().equals(idCompetence))
                    .anyMatch(o -> o.getDateExpiration() == null
                            || !o.getDateExpiration().isBefore(datePrevue));
            if (!satisfaite) {
                manquantes.add(competenceRepository.findById(idCompetence)
                        .map(Competence::getLibelle).orElse("Compétence " + idCompetence));
            }
        }
        if (!manquantes.isEmpty()) {
            throw new DonneeInvalideException("competences-manquantes",
                    "Compétences manquantes ou expirées : " + String.join(", ", manquantes),
                    Map.of("competences", manquantes));
        }
    }

    // ── typeDe() ─────────────────────────────────────────────────────────────

    /**
     * Exécute typeDe().
     */

    private TypeIntervention typeDe(Intervention intervention) {
        return typeRepository.findByCode(intervention.getCodeTypeIntervention())
                .orElseThrow(() -> new DonneeInvalideException("type-intervention-inconnu",
                        "Le code de type d'intervention « "
                                + intervention.getCodeTypeIntervention() + " » est inconnu"));
    }

    // ── requiredResponsable() ────────────────────────────────────────────────

    /**
     * Exécute requiredResponsable().
     */

    private Affectation requiredResponsable(Integer idIntervention) {
        return affectationRepository.findByIdInterventionAndEstResponsableTrue(idIntervention)
                .orElseThrow(() -> new ConflitMetierException("responsable-absent",
                        "Cette intervention n'a pas de technicien responsable"));
    }

    // ── requiredTechnicien() ─────────────────────────────────────────────────

    /**
     * Exécute requiredTechnicien().
     */

    private void requiredTechnicien(Integer id) {
        if (!technicienRepository.existsById(id)) {
            throw new RessourceIntrouvableException("Technicien", id);
        }
    }

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Exécute required().
     */

    private Intervention required(Integer id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Intervention", id));
    }
}

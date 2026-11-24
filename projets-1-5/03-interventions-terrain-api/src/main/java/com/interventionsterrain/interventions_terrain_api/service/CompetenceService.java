package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.CompetenceMapper;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des compétences (F3).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles métier décrites dans l'en-tête, piloter les transactions
 *                et orchestrer repositories et mappers.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : CompetenceMapper, CompetenceRepository, ExigenceCompetenceRepository,
 *                ObtentionCompetenceRepository
 * UTILISÉ PAR  : CompetenceController.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class CompetenceService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Accès en base à la table associée à competence. */
    private final CompetenceRepository competenceRepository;
    /** Accès en base à la table associée à obtention. */
    private final ObtentionCompetenceRepository obtentionRepository;
    /** Accès en base à la table associée à exigence. */
    private final ExigenceCompetenceRepository exigenceRepository;
    /** Conversion entité ↔ DTO, sans logique métier. */
    private final CompetenceMapper mapper;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    public CompetenceService(CompetenceRepository competenceRepository,
                             ObtentionCompetenceRepository obtentionRepository,
                             ExigenceCompetenceRepository exigenceRepository,
                             CompetenceMapper mapper) {
        this.competenceRepository = competenceRepository;
        this.obtentionRepository = obtentionRepository;
        this.exigenceRepository = exigenceRepository;
        this.mapper = mapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    public List<CompetenceResponse> findAll() {
        return competenceRepository.findAllByOrderByLibelleAsc().stream().map(mapper::toResponse).toList();
    }

    public CompetenceResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    @Transactional
    public CompetenceResponse create(CreateCompetenceRequest request) {
        checkLibelle(request.libelle(), null);
        return mapper.toResponse(competenceRepository.save(
                new Competence(request.libelle(), request.description())));
    }

    @Transactional
    public CompetenceResponse update(Integer id, UpdateCompetenceRequest request) {
        Competence competence = required(id);
        checkLibelle(request.libelle(), id);
        competence.modifier(request.libelle(), request.description());
        return mapper.toResponse(competence);
    }

    @Transactional
    public void delete(Integer id) {
        Competence competence = required(id);
        if (obtentionRepository.existsByIdCompetence(id) || exigenceRepository.existsByIdIdCompetence(id)) {
            throw new ConflitMetierException("competence-non-supprimable",
                    "Cette compétence est encore obtenue par des techniciens ou exigée par un type");
        }
        competenceRepository.delete(competence);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    private void checkLibelle(String libelle, Integer idCourant) {
        competenceRepository.findByLibelle(libelle).filter(other -> {
            Integer id = other.getIdCompetence();
            return idCourant == null || !id.equals(idCourant);
        }).ifPresent(other -> {
            throw new ConflitMetierException("competence-libelle-deja-utilise",
                    "Le libellé « " + libelle + " » est déjà utilisé");
        });
    }

    private Competence required(Integer id) {
        return competenceRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Compétence", id));
    }
}

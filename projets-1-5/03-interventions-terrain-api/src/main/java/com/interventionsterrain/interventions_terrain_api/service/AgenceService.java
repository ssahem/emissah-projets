package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienEffectifResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.AgenceMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD des agences (F1) et effectif d'une agence (F6). */
@Service
@Transactional(readOnly = true)
public class AgenceService {

    private final AgenceRepository agenceRepository;
    private final EquipeRepository equipeRepository;
    private final TechnicienRepository technicienRepository;
    private final ObtentionCompetenceRepository obtentionRepository;
    private final AgenceMapper mapper;
    private final Clock clock;

    public AgenceService(AgenceRepository agenceRepository, EquipeRepository equipeRepository,
                         TechnicienRepository technicienRepository,
                         ObtentionCompetenceRepository obtentionRepository,
                         AgenceMapper mapper, Clock clock) {
        this.agenceRepository = agenceRepository;
        this.equipeRepository = equipeRepository;
        this.technicienRepository = technicienRepository;
        this.obtentionRepository = obtentionRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<AgenceResponse> findAll() {
        return agenceRepository.findAllByOrderByVilleAscNomAsc().stream().map(mapper::toResponse).toList();
    }

    public AgenceResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F6 : les techniciens de l'agence via equipe.id_agence, triés par nom, avec leurs compétences valides. */
    public List<TechnicienEffectifResponse> findTechniciens(Integer id) {
        required(id);
        List<Integer> idsEquipe = equipeRepository.findAllByIdAgence(id).stream()
                .map(Equipe::getIdEquipe).toList();
        if (idsEquipe.isEmpty()) {
            return List.of();
        }
        LocalDate aujourdHui = LocalDate.now(clock);
        return technicienRepository.findAllByIdEquipeInOrderByNomAsc(idsEquipe).stream()
                .map(t -> new TechnicienEffectifResponse(
                        t.getIdTechnicien(), t.getNom(), t.getPrenom(), t.getMatricule(),
                        obtentionRepository.countCompetencesValides(t.getIdTechnicien(), aujourdHui)))
                .toList();
    }

    @Transactional
    public AgenceResponse create(CreateAgenceRequest request) {
        if (agenceRepository.findByNom(request.nom()).isPresent()) {
            throw new ConflitMetierException("agence-nom-deja-utilise",
                    "Le nom d'agence « " + request.nom() + " » est déjà utilisé");
        }
        String adresse = request.adresse() != null && !request.adresse().isBlank()
                ? request.adresse() : request.ville();
        Agence agence = agenceRepository.save(
                new Agence(request.nom(), adresse, request.ville(), request.responsable()));
        return mapper.toResponse(agence);
    }

    @Transactional
    public AgenceResponse update(Integer id, UpdateAgenceRequest request) {
        Agence agence = required(id);
        agenceRepository.findByNom(request.nom())
                .filter(other -> !other.getIdAgence().equals(id))
                .ifPresent(other -> {
                    throw new ConflitMetierException("agence-nom-deja-utilise",
                            "Le nom d'agence « " + request.nom() + " » est déjà utilisé");
                });
        boolean figee = equipeRepository.existsByIdAgence(id);
        if (figee && !agence.getVille().equals(request.ville())) {
            throw new ConflitMetierException("agence-ville-figee",
                    "La ville ne peut plus changer dès qu'une équipe est rattachée à l'agence");
        }
        agence.modifier(request.nom(), request.responsable(), figee ? agence.getVille() : request.ville());
        return mapper.toResponse(agence);
    }

    @Transactional
    public void delete(Integer id) {
        Agence agence = required(id);
        if (equipeRepository.existsByIdAgence(id)) {
            throw new ConflitMetierException("agence-non-supprimable",
                    "Cette agence possède encore des équipes");
        }
        agenceRepository.delete(agence);
    }

    private Agence required(Integer id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Agence", id));
    }
}

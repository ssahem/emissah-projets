package com.flottevehicules.flotte_vehicules_api.service;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationSalarieResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.SalarieResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.AffectationMapper;
import com.flottevehicules.flotte_vehicules_api.mapper.SalarieMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SalarieRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des salariés (F3) et consultations de leurs affectations (F7).
 *
 * COUCHE       : Service
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class SalarieService {

    private final SalarieRepository salarieRepository;
    private final AffectationRepository affectationRepository;
    private final SalarieMapper mapper;
    private final AffectationMapper affectationMapper;

    public SalarieService(SalarieRepository salarieRepository,
                          AffectationRepository affectationRepository,
                          SalarieMapper mapper, AffectationMapper affectationMapper) {
        this.salarieRepository = salarieRepository;
        this.affectationRepository = affectationRepository;
        this.mapper = mapper;
        this.affectationMapper = affectationMapper;
    }

    /** F3 : salariés triés par nom. */
    public List<SalarieResponse> findAll() {
        return salarieRepository.findAllByOrderByNomAsc().stream().map(mapper::toResponse).toList();
    }

    /** F3 : un salarié par identifiant ; 404 si inconnu. */
    public SalarieResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F3 : crée un salarié ; matricule et email doivent être uniques. */
    @Transactional
    public SalarieResponse create(CreateSalarieRequest request) {
        if (salarieRepository.findByMatricule(request.matricule()).isPresent()) {
            throw new ConflitMetierException("salarie-matricule-deja-utilise",
                    "Le matricule « " + request.matricule() + " » est déjà utilisé");
        }
        if (salarieRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflitMetierException("salarie-email-deja-utilise",
                    "L'adresse électronique « " + request.email() + " » est déjà utilisée");
        }
        return mapper.toResponse(salarieRepository.save(new Salarie(
                request.nom(), request.prenom(), request.email(),
                request.matricule(), request.service())));
    }

    /** F3 : nom, prénom, email et service modifiables ; matricule figé. */
    @Transactional
    public SalarieResponse update(Integer id, UpdateSalarieRequest request) {
        Salarie salarie = required(id);
        salarieRepository.findByEmail(request.email())
                .filter(other -> !other.getIdSalarie().equals(id))
                .ifPresent(other -> {
                    throw new ConflitMetierException("salarie-email-deja-utilise",
                            "L'adresse électronique « " + request.email() + " » est déjà utilisée");
                });
        salarie.modifier(request.nom(), request.prenom(), request.email(), request.service());
        return mapper.toResponse(salarieRepository.save(salarie));
    }

    /** F3 : suppression refusée si le salarié a une affectation en cours. */
    @Transactional
    public void delete(Integer id) {
        Salarie salarie = required(id);
        if (affectationRepository.existsByIdSalarieAndDateFinIsNull(id)) {
            throw new ConflitMetierException("salarie-non-supprimable",
                    "Ce salarié a encore une affectation en cours");
        }
        salarieRepository.delete(salarie);
    }

    /** F7 : affectations d'un salarié, de la plus récente à la plus ancienne. */
    public List<AffectationSalarieResponse> affectations(Integer id) {
        required(id);
        return affectationRepository.findAllByIdSalarieOrderByDateDebutDesc(id).stream()
                .map(affectationMapper::toSalarieResponse)
                .toList();
    }

    private Salarie required(Integer id) {
        return salarieRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Salarié", id));
    }
}

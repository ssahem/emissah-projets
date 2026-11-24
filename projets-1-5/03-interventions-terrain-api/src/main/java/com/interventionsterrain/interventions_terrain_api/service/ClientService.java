package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.ClientResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.HistoriqueClientResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.ClientMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD des clients (F4) et historique de leurs interventions (F8). */
@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final InterventionRepository interventionRepository;
    private final AffectationRepository affectationRepository;
    private final TechnicienRepository technicienRepository;
    private final HistoriqueStatutRepository historiqueRepository;
    private final StatutRepository statutRepository;
    private final ClientMapper mapper;

    public ClientService(ClientRepository clientRepository, InterventionRepository interventionRepository,
                         AffectationRepository affectationRepository,
                         TechnicienRepository technicienRepository,
                         HistoriqueStatutRepository historiqueRepository,
                         StatutRepository statutRepository, ClientMapper mapper) {
        this.clientRepository = clientRepository;
        this.interventionRepository = interventionRepository;
        this.affectationRepository = affectationRepository;
        this.technicienRepository = technicienRepository;
        this.historiqueRepository = historiqueRepository;
        this.statutRepository = statutRepository;
        this.mapper = mapper;
    }

    public List<ClientResponse> findAll() {
        return clientRepository.findAllByOrderByNomAsc().stream().map(mapper::toResponse).toList();
    }

    public ClientResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** F8 : interventions du plus récent au plus ancien, avec responsable, type et statut. */
    public List<HistoriqueClientResponse> findHistorique(Integer id) {
        required(id);
        return interventionRepository.findAllByIdClientOrderByDateDemandeDesc(id).stream()
                .map(this::toHistorique)
                .toList();
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        checkEmail(request.email(), null);
        String email = request.email() != null && !request.email().isBlank()
                ? request.email() : "client-" + System.nanoTime() + "@example.test";
        Client client = clientRepository.save(new Client(
                request.nom(),
                request.prenom() != null ? request.prenom() : "",
                email,
                request.telephone(),
                request.adresse(),
                request.commune()));
        return mapper.toResponse(client);
    }

    @Transactional
    public ClientResponse update(Integer id, UpdateClientRequest request) {
        Client client = required(id);
        checkEmail(request.email(), id);
        client.modifier(request.nom(),
                request.prenom() != null ? request.prenom() : "",
                request.email(), request.telephone(), request.adresse(), request.commune());
        return mapper.toResponse(client);
    }

    @Transactional
    public void delete(Integer id) {
        Client client = required(id);
        if (interventionRepository.existsByIdClient(id)) {
            throw new ConflitMetierException("client-non-supprimable",
                    "Ce client possède encore des interventions");
        }
        clientRepository.delete(client);
    }

    private HistoriqueClientResponse toHistorique(Intervention intervention) {
        Affectation responsable = affectationRepository
                .findByIdInterventionAndEstResponsableTrue(intervention.getIdIntervention())
                .orElse(null);
        Technicien technicien = responsable != null
                ? technicienRepository.findById(responsable.getIdTechnicien()).orElse(null) : null;
        StatutIntervention statut = statutCourant(responsable);
        return new HistoriqueClientResponse(
                intervention.getIdIntervention(), intervention.getDateDemande(),
                intervention.getDebutPrevu(), intervention.getFinPrevue(),
                intervention.getCodeTypeIntervention(),
                responsable != null ? responsable.getIdTechnicien() : null,
                technicien != null ? technicien.getNom() + " " + technicien.getPrenom() : null,
                statut != null ? statut.name() : null);
    }

    private StatutIntervention statutCourant(Affectation responsable) {
        if (responsable == null) {
            return null;
        }
        return historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(
                        responsable.getIdAffectation())
                .flatMap(h -> statutRepository.findById(h.getIdStatut()))
                .map(s -> StatutIntervention.fromLibelle(s.getLibelle()))
                .filter(Objects::nonNull)
                .orElse(StatutIntervention.PLANIFIEE);
    }

    private void checkEmail(String email, Integer idCourant) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (clientRepository.existsByEmail(email)) {
            // Une mise à jour avec le même email est autorisée : la valeur courante est vérifiée côté entité.
            if (idCourant == null || !email.equals(required(idCourant).getEmail())) {
                throw new ConflitMetierException("client-email-deja-utilise",
                        "Cet email est déjà utilisé");
            }
        }
    }

    private Client required(Integer id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Client", id));
    }
}

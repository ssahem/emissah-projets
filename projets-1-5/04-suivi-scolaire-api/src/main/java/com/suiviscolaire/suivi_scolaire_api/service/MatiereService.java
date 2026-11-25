package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.MatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.MatiereMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des matières (F3).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles d'unicité, de coefficient et de suppression.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : MatiereRepository, EnseignementRepository, TrimestreRepository,
 *                MatiereMapper.
 * UTILISÉ PAR  : MatiereController.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class MatiereService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final MatiereRepository matiereRepository;
    private final EnseignementRepository enseignementRepository;
    private final TrimestreRepository trimestreRepository;
    private final MatiereMapper mapper;

    /** Spring injecte les dépendances par constructeur. */
    public MatiereService(MatiereRepository matiereRepository,
                          EnseignementRepository enseignementRepository,
                          TrimestreRepository trimestreRepository, MatiereMapper mapper) {
        this.matiereRepository = matiereRepository;
        this.enseignementRepository = enseignementRepository;
        this.trimestreRepository = trimestreRepository;
        this.mapper = mapper;
    }

    /** Liste les matières triées par libellé. */

    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Exécute findAll().
     */

    public List<MatiereResponse> findAll() {
        return matiereRepository.findAllByOrderByLibelleAsc().stream().map(mapper::toResponse).toList();
    }

    /** Consulte une matière par identifiant (404 si inconnue). */

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Exécute findById().
     */

    public MatiereResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** Crée une matière avec un libellé unique et un coefficient strictement positif. */

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Exécute create().
     */

    @Transactional
    public MatiereResponse create(CreateMatiereRequest request) {
        if (matiereRepository.findByLibelle(request.libelle()).isPresent()) {
            throw new ConflitMetierException("matiere-libelle-deja-utilise",
                    "Le libellé « " + request.libelle() + " » est déjà utilisé");
        }
        return mapper.toResponse(matiereRepository.save(new Matiere(
                request.libelle(), request.code(), request.coefficient())));
    }

    /** Modifie le libellé et le coefficient tant qu'aucun trimestre n'est clôturé. */

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Exécute update().
     */

    @Transactional
    public MatiereResponse update(Integer id, UpdateMatiereRequest request) {
        Matiere matiere = required(id);
        matiereRepository.findByLibelle(request.libelle())
                .filter(other -> !other.getIdMatiere().equals(id))
                .ifPresent(other -> {
                    throw new ConflitMetierException("matiere-libelle-deja-utilise",
                            "Le libellé « " + request.libelle() + " » est déjà utilisé");
                });
        if (trimestreRepository.existsByEstClotureTrue()) {
            throw new ConflitMetierException("matiere-coefficient-fige",
                    "Le coefficient n'est plus modifiable : un trimestre est déjà clôturé");
        }
        matiere.modifier(request.libelle(), request.coefficient());
        return mapper.toResponse(matiere);
    }

    /** Refuse la suppression d'une matière ayant des notes ou un professeur affecté. */

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Exécute delete().
     */

    @Transactional
    public void delete(Integer id) {
        Matiere matiere = required(id);
        if (enseignementRepository.existsByIdMatiere(id)) {
            throw new ConflitMetierException("matiere-non-supprimable",
                    "Cette matière possède encore un professeur affecté ou des notes");
        }
        matiereRepository.delete(matiere);
    }

    /** Retourne la matière ou lève une erreur métier 404. */

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Exécute required().
     */

    public Matiere required(Integer id) {
        return matiereRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Matiere", id));
    }
}

package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.ClasseMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des classes et règles d'effectif (F1).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles de création, modification et suppression des classes.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : ClasseRepository, InscriptionRepository, ClasseMapper.
 * UTILISÉ PAR  : ClasseController.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class ClasseService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final ClasseRepository classeRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClasseMapper mapper;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances par constructeur. */
    public ClasseService(ClasseRepository classeRepository,
                         InscriptionRepository inscriptionRepository, ClasseMapper mapper) {
        this.classeRepository = classeRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.mapper = mapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════


    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Liste les classes triées par niveau puis par nom, avec leur effectif
     * courant.
     */

    public List<ClasseResponse> findAll() {
        return classeRepository.findAllByOrderByIdNiveauAscNomAsc().stream().map(mapper::toResponse).toList();
    }


    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Consulte une classe par identifiant (404 si inconnue).
     */

    public ClasseResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }


    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée une classe ; le nom doit être unique dans le niveau (409).
     */

    @Transactional
    public ClasseResponse create(CreateClasseRequest request) {
        // ── Étape 1/2 : refuser un doublon de nom dans le même niveau ────────
        if (classeRepository.findByNomAndIdNiveau(request.nom(), request.idNiveau()).isPresent()) {
            throw new ConflitMetierException("classe-nom-deja-utilise",
                    "Une classe nommée « " + request.nom() + " » existe déjà dans ce niveau");
        }
        // ── Étape 2/2 : persister la classe ──────────────────────────────────
        return mapper.toResponse(classeRepository.save(new Classe(
                request.nom(), request.idAnnee(), request.idNiveau(), request.effectifMax())));
    }


    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Modifie l'effectif maximal sans jamais descendre sous l'effectif actuel
     * (F1).
     */

    @Transactional
    public ClasseResponse update(Integer id, UpdateClasseRequest request) {
        Classe classe = required(id);
        long effectifActuel = inscriptionRepository.countByIdClasseAndDateFinIsNull(id);
        if (request.effectifMax() < effectifActuel) {
            throw new ConflitMetierException("classe-effectif-insuffisant",
                    "L'effectif maximal ne peut pas être inférieur à l'effectif actuel (" + effectifActuel + ")",
                    Map.of("effectifActuel", effectifActuel));
        }
        classe.modifierEffectifMax(request.effectifMax());
        return mapper.toResponse(classe);
    }


    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Supprime une classe vide ; une classe avec élèves est refusée (409).
     */

    @Transactional
    public void delete(Integer id) {
        Classe classe = required(id);
        if (inscriptionRepository.countByIdClasseAndDateFinIsNull(id) > 0) {
            throw new ConflitMetierException("classe-non-supprimable",
                    "Cette classe possède encore des élèves");
        }
        classeRepository.delete(classe);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════


    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Retourne la classe ou lève une erreur métier 404.
     */

    public Classe required(Integer id) {
        return classeRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", id));
    }
}

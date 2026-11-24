package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NoteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesMatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.NoteMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TypeEvaluationRepository;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des notes (F5, avec A-01 et A-03).
 *
 * COUCHE       : Service
 * RÔLE         : créer, consulter, modifier et supprimer des notes hors trimestre clôturé.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : NoteRepository, InscriptionRepository, EnseignementRepository,
 *                TrimestreLookup, NoteMapper.
 * UTILISÉ PAR  : NoteController.
 * NE FAIT PAS  : de HTTP ni de FK synthétique (numero_inscription et numero_trimestre sont
 *                résolus en service).
 */
@Service
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EnseignementRepository enseignementRepository;
    private final EleveRepository eleveRepository;
    private final TypeEvaluationRepository typeEvaluationRepository;
    private final MatiereRepository matiereRepository;
    private final TrimestreLookup trimestreLookup;
    private final NoteMapper mapper;

    /** Spring injecte les dépendances par constructeur. */
    public NoteService(NoteRepository noteRepository, InscriptionRepository inscriptionRepository,
                       EnseignementRepository enseignementRepository, EleveRepository eleveRepository,
                       TypeEvaluationRepository typeEvaluationRepository,
                       MatiereRepository matiereRepository,
                       TrimestreLookup trimestreLookup, NoteMapper mapper) {
        this.noteRepository = noteRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.enseignementRepository = enseignementRepository;
        this.eleveRepository = eleveRepository;
        this.typeEvaluationRepository = typeEvaluationRepository;
        this.matiereRepository = matiereRepository;
        this.trimestreLookup = trimestreLookup;
        this.mapper = mapper;
    }

    /** Liste les notes groupées par matière (F5 : forme par défaut de GET /note/list). */
    public List<NotesMatiereResponse> findAll() {
        Map<Integer, List<NoteResponse>> groupes = new LinkedHashMap<>();
        noteRepository.findAllByOrderByIdEnseignementAscNumeroTrimestreAsc()
                .forEach(note -> groupes
                        .computeIfAbsent(note.getIdEnseignement(), cle -> new ArrayList<>())
                        .add(mapper.toResponse(note)));
        return groupes.entrySet().stream().map(entree -> {
            Integer idEnseignement = entree.getKey();
            Integer idMatiere = enseignementRepository.findById(idEnseignement)
                    .map(e -> e.getIdMatiere()).orElse(null);
            String libelle = idMatiere == null ? null
                    : matiereRepository.findById(idMatiere).map(m -> m.getLibelle()).orElse(null);
            return new NotesMatiereResponse(idMatiere, libelle, entree.getValue());
        }).toList();
    }

    /** Consulte une note par identifiant (404 si inconnue). */
    public NoteResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** Crée une note : l'élève, la matière, le type et le trimestre ouvert sont vérifiés. */
    @Transactional
    public NoteResponse create(CreateNoteRequest request) {
        // ── Étape 1/4 : le trimestre doit exister et rester ouvert ──
        trimestreLookup.exigible(request.trimestre(), "La saisie de note");
        // ── Étape 2/4 : résoudre l'élève via son numéro d'inscription unique (A-01) ──
        Eleve eleve = eleveRepository.findById(request.idEleve())
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", request.idEleve()));
        Inscription inscription = inscriptionRepository.findByIdEleveAndDateFinIsNull(request.idEleve())
                .orElseThrow(() -> new DonneeInvalideException("inscription-manquante",
                        "L'élève " + eleve.getNom() + " n'a aucune inscription courante"));
        // ── Étape 3/4 : retrouver l'enseignement de la matière dans la classe de l'élève ──
        Enseignement enseignement = enseignementRepository
                .findByIdClasseAndIdMatiere(inscription.getIdClasse(), request.idMatiere())
                .orElseThrow(() -> new DonneeInvalideException("matiere-non-enseignee",
                        "La matière n'est pas enseignée dans la classe de cet élève"));
        if (!typeEvaluationRepository.existsById(request.idTypeEval())) {
            throw new DonneeInvalideException("type-evaluation-inconnu",
                    "Le type d'évaluation " + request.idTypeEval() + " est inconnu");
        }
        // ── Étape 4/4 : persister la note ──
        Note note = noteRepository.save(new Note(request.valeur(), request.trimestre(),
                request.idTypeEval(), enseignement.getIdEnseignement(), inscription.getNumeroInscription()));
        return mapper.toResponse(note);
    }

    /** Modifie la valeur d'une note tant que le trimestre n'est pas clôturé. */
    @Transactional
    public NoteResponse update(Integer id, UpdateNoteRequest request) {
        Note note = required(id);
        trimestreLookup.exigible(note.getNumeroTrimestre(), "La modification de note");
        note.modifierValeur(request.valeur());
        return mapper.toResponse(note);
    }

    /** Supprime une note tant que le trimestre n'est pas clôturé. */
    @Transactional
    public void delete(Integer id) {
        Note note = required(id);
        trimestreLookup.exigible(note.getNumeroTrimestre(), "La suppression de note");
        noteRepository.delete(note);
    }

    /** Retourne la note ou lève une erreur métier 404. */
    public Note required(Integer id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Note", id));
    }

    /** Construit l'en-tête Location d'une note créée. */
    public URI location(Integer id) {
        return URI.create("/suivi-scolaire/note/get/" + id);
    }

    /** Valeur minimale autorisée pour une note (0). */
    public BigDecimal valeurMinimale() { return BigDecimal.ZERO; }
}

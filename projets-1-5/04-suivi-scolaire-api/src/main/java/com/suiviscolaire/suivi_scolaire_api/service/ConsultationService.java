package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.response.EffectifClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EleveResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NoteResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesEleveResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.NotesMatiereResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.PageResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.mapper.EleveMapper;
import com.suiviscolaire.suivi_scolaire_api.mapper.NoteMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultations et recherches (F6 à F10).
 *
 * COUCHE       : Service
 * RÔLE         : lire les élèves d'une classe, les notes d'un élève ou d'une matière,
 *                rechercher et paginer.
 * TRANSACTION  : lecture seule.
 * DÉPEND DE    : repositories de lecture, EleveMapper, NoteMapper, TrimestreLookup.
 * UTILISÉ PAR  : ConsultationController.
 * NE FAIT PAS  : d'écriture ni de HTTP.
 */
@Service
@Transactional(readOnly = true)
public class ConsultationService {

    private static final int TAILLE_MAX = 100;
    private static final int TAILLE_DEFAUT = 30;

    private final ClasseRepository classeRepository;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;
    private final EleveMapper eleveMapper;
    private final NoteMapper noteMapper;
    private final TrimestreLookup trimestreLookup;

    /** Spring injecte les dépendances par constructeur. */
    public ConsultationService(ClasseRepository classeRepository, EleveRepository eleveRepository,
                               InscriptionRepository inscriptionRepository,
                               EnseignementRepository enseignementRepository,
                               MatiereRepository matiereRepository, NoteRepository noteRepository,
                               EleveMapper eleveMapper, NoteMapper noteMapper,
                               TrimestreLookup trimestreLookup) {
        this.classeRepository = classeRepository;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.noteRepository = noteRepository;
        this.eleveMapper = eleveMapper;
        this.noteMapper = noteMapper;
        this.trimestreLookup = trimestreLookup;
    }

    /** F6 : élèves d'une classe triés par nom puis prénom, avec effectif courant et maximal. */
    public EffectifClasseResponse elevesDeClasse(Integer idClasse) {
        Classe classe = classeRepository.findById(idClasse)
                .orElseThrow(() -> new com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException("Classe", idClasse));
        List<EleveResponse> eleves = inscriptionRepository.findAllByIdClasseAndDateFinIsNull(idClasse)
                .stream()
                .map(i -> eleveRepository.findById(i.getIdEleve()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .sorted(Comparator.comparing(Eleve::getNom).thenComparing(Eleve::getPrenom))
                .map(eleveMapper::toResponse)
                .toList();
        return new EffectifClasseResponse(classe.getIdClasse(), classe.getNom(), eleves.size(),
                classe.getEffectifMax(), eleves);
    }

    /** F7 : notes d'un élève, groupées par matière ; un trimestre absent signifie « toutes ». */
    public List<NotesMatiereResponse> notesEleve(Integer idEleve, Integer trimestre) {
        eleveRepository.findById(idEleve)
                .orElseThrow(() -> new com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException("Eleve", idEleve));
        if (trimestre != null) {
            trimestreLookup.required(trimestre);
        }
        Inscription inscription = inscriptionRepository.findByIdEleveAndDateFinIsNull(idEleve)
                .orElseThrow(() -> new DonneeInvalideException("inscription-manquante",
                        "L'élève n'a aucune inscription courante"));
        List<NoteResponse> notes = noteRepository
                .findByNumeroInscription(inscription.getNumeroInscription(), trimestre).stream()
                .map(noteMapper::toResponse).toList();
        Map<Integer, List<NoteResponse>> groupes = new LinkedHashMap<>();
        notes.forEach(n -> groupes.computeIfAbsent(n.idMatiere(), k -> new java.util.ArrayList<>()).add(n));
        return groupes.entrySet().stream()
                .map(e -> new NotesMatiereResponse(e.getKey(),
                        matiereRepository.findById(e.getKey()).map(m -> m.getLibelle()).orElse(null),
                        e.getValue()))
                .toList();
    }

    /** F8 : notes d'une matière dans une classe avec moyenne de classe ; 400 si non enseignée. */
    public NotesClasseResponse notesMatiereClasse(Integer idClasse, Integer idMatiere, Integer trimestre) {
        trimestreLookup.required(trimestre);
        Classe classe = classeRepository.findById(idClasse)
                .orElseThrow(() -> new com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException("Classe", idClasse));
        matiereRepository.findById(idMatiere)
                .orElseThrow(() -> new com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException("Matiere", idMatiere));
        Enseignement enseignement = enseignementRepository.findByIdClasseAndIdMatiere(idClasse, idMatiere)
                .orElseThrow(() -> new DonneeInvalideException("matiere-non-enseignee",
                        "La matière n'est pas enseignée dans cette classe"));
        List<Note> notes = noteRepository.findByIdEnseignementAndTrimestre(
                enseignement.getIdEnseignement(), trimestre);
        Map<String, List<Note>> parInscription = new LinkedHashMap<>();
        notes.forEach(n -> parInscription
                .computeIfAbsent(n.getNumeroInscription(), k -> new java.util.ArrayList<>()).add(n));
        List<NotesEleveResponse> eleves = parInscription.entrySet().stream().map(e -> {
            Inscription inscription = inscriptionRepository.findByNumeroInscription(e.getKey()).orElse(null);
            Eleve eleve = inscription == null ? null
                    : eleveRepository.findById(inscription.getIdEleve()).orElse(null);
            List<NoteResponse> responseNotes = e.getValue().stream().map(noteMapper::toResponse).toList();
            return new NotesEleveResponse(eleve == null ? null : eleve.getIdEleve(),
                    eleve == null ? null : eleve.getNom(),
                    eleve == null ? null : eleve.getPrenom(),
                    moyenne(e.getValue()), responseNotes);
        }).toList();
        return new NotesClasseResponse(classe.getIdClasse(), idMatiere, trimestre,
                moyenne(notes), eleves);
    }

    /** F9 : recherche partielle et insensible à la casse ; une recherche vide est refusée. */
    public List<EleveResponse> rechercher(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new DonneeInvalideException("recherche-vide",
                    "Le critère de recherche ne peut pas être vide");
        }
        return eleveRepository.searchByNom(nom.trim()).stream().map(eleveMapper::toResponse).toList();
    }

    /** F10 : page de notes, triée selon le paramètre sort ; taille plafonnée à 100. */
    public PageResponse<NoteResponse> notesPaginees(int page, int size, String sort) {
        int pageSafe = Math.max(page, 0);
        int sizeSafe = Math.min(Math.max(size, 1), TAILLE_MAX);
        String propriete = "valeur,desc".equals(sort) ? "valeur" : "idNote";
        Sort.Direction direction = "valeur,desc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<Note> resultat = noteRepository.findAllBy(PageRequest.of(pageSafe, sizeSafe,
                Sort.by(direction, propriete)));
        return new PageResponse<>(resultat.getContent().stream().map(noteMapper::toResponse).toList(),
                resultat.getNumber(), resultat.getSize(), resultat.getTotalElements(), resultat.getTotalPages());
    }

    /** Calcule la moyenne arithmétique arrondie au centième. */
    private BigDecimal moyenne(List<Note> notes) {
        if (notes.isEmpty()) {
            return null;
        }
        return notes.stream().map(Note::getValeur)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notes.size()), 2, RoundingMode.HALF_UP);
    }
}

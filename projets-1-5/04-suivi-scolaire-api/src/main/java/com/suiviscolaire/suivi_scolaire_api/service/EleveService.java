package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.request.ChangeClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ChangementClasseResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.EleveResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.EleveMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des élèves, inscriptions et changements de classe (F2, F11).
 *
 * COUCHE       : Service
 * RÔLE         : gérer les fiches et les affectations courantes ; conserver l'historique des
 *                inscriptions.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : EleveRepository, InscriptionRepository, ClasseRepository, EleveMapper, Clock.
 * UTILISÉ PAR  : EleveController, ConsultationService, RestitutionService.
 * NE FAIT PAS  : de HTTP ni de SQL direct.
 */
@Service
@Transactional(readOnly = true)
public class EleveService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ClasseRepository classeRepository;
    private final EleveMapper mapper;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances par constructeur. */
    public EleveService(EleveRepository eleveRepository, InscriptionRepository inscriptionRepository,
                        ClasseRepository classeRepository, EleveMapper mapper, Clock clock) {
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.classeRepository = classeRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    /** Liste les élèves triés par nom puis prénom, avec leur classe courante. */
    public List<EleveResponse> findAll() {
        return eleveRepository.findAllByOrderByNomAscPrenomAsc().stream().map(mapper::toResponse).toList();
    }

    /** Consulte une fiche élève par identifiant (404 si inconnue). */
    public EleveResponse findById(Integer id) {
        return mapper.toResponse(required(id));
    }

    /** Crée un élève dans une classe non pleine, avec une date de naissance passée. */
    @Transactional
    public EleveResponse create(CreateEleveRequest request) {
        // ── Étape 1/3 : la date de naissance doit être dans le passé ──
        if (!request.dateNaissance().isBefore(LocalDate.now(clock))) {
            throw new DonneeInvalideException("date-naissance-future",
                    "La date de naissance doit être dans le passé",
                    Map.of("dateNaissance", request.dateNaissance().toString()));
        }
        // ── Étape 2/3 : la classe d'accueil ne doit pas être pleine ──
        Classe classe = classeRepository.findById(request.idClasse())
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", request.idClasse()));
        verifierPlace(classe);
        // ── Étape 3/3 : créer la fiche et l'inscription courante ──
        Eleve eleve = eleveRepository.save(new Eleve(
                request.nom(), request.prenom(), request.dateNaissance()));
        inscriptionRepository.save(new Inscription(
                LocalDate.now(clock), eleve.getIdEleve(), classe.getIdClasse(),
                "INS-" + LocalDate.now(clock).getYear() + "-" + String.format("%04d", eleve.getIdEleve())));
        return mapper.toResponse(eleve);
    }

    /** Modifie nom/prénom et, si besoin, la classe courante ; la date de naissance est figée. */
    @Transactional
    public EleveResponse update(Integer id, UpdateEleveRequest request) {
        Eleve eleve = required(id);
        Inscription courante = inscriptionRepository.findByIdEleveAndDateFinIsNull(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Inscription courante", id));
        if (!courante.getIdClasse().equals(request.idClasse())) {
            Classe destination = classeRepository.findById(request.idClasse())
                    .orElseThrow(() -> new RessourceIntrouvableException("Classe", request.idClasse()));
            verifierPlace(destination);
            courante.clore(LocalDate.now(clock));
            inscriptionRepository.save(new Inscription(LocalDate.now(clock), id,
                    destination.getIdClasse(), courante.getNumeroInscription() + "-BIS"));
        }
        eleve.modifier(request.nom(), request.prenom());
        return mapper.toResponse(eleve);
    }

    /** Refuse la suppression d'un élève ayant des notes (F2, A-01). */
    @Transactional
    public void delete(Integer id) {
        Eleve eleve = required(id);
        List<Inscription> inscriptions = inscriptionRepository.findAllByIdEleveOrderByDateInscriptionAsc(id);
        boolean aDesNotes = inscriptions.stream()
                .anyMatch(i -> inscriptionRepository.hasNotes(i.getNumeroInscription()));
        if (aDesNotes) {
            throw new ConflitMetierException("eleve-non-supprimable",
                    "Cet élève possède encore des notes");
        }
        inscriptions.forEach(inscriptionRepository::delete);
        eleveRepository.delete(eleve);
    }

    /** Réaffecte un élève dans une classe du même niveau non pleine (F11). */
    @Transactional
    public ChangementClasseResponse changerClasse(Integer id, ChangeClasseRequest request) {
        Eleve eleve = required(id);
        Inscription courante = inscriptionRepository.findByIdEleveAndDateFinIsNull(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Inscription courante", id));
        Classe source = classeRepository.findById(courante.getIdClasse())
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", courante.getIdClasse()));
        Classe destination = classeRepository.findById(request.idClasseDestination())
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", request.idClasseDestination()));
        if (!source.getIdNiveau().equals(destination.getIdNiveau())) {
            throw new DonneeInvalideException("niveau-different",
                    "La classe de destination doit être du même niveau",
                    Map.of("niveauSource", source.getIdNiveau(),
                            "niveauDestination", destination.getIdNiveau()));
        }
        verifierPlace(destination);
        courante.clore(LocalDate.now(clock));
        Inscription nouvelle = inscriptionRepository.save(new Inscription(
                LocalDate.now(clock), eleve.getIdEleve(), destination.getIdClasse(),
                courante.getNumeroInscription() + "-A"));
        return new ChangementClasseResponse(eleve.getIdEleve(), source.getIdClasse(),
                destination.getIdClasse(), courante.getDateFin(), nouvelle.getDateInscription(),
                inscriptionRepository.findAllByIdEleveOrderByDateInscriptionAsc(id).size());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne l'élève ou lève une erreur métier 404. */
    public Eleve required(Integer id) {
        return eleveRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Eleve", id));
    }

    /** Vérifie qu'une inscription courante existe encore dans la classe (capacité). */
    private void verifierPlace(Classe classe) {
        long effectif = inscriptionRepository.countByIdClasseAndDateFinIsNull(classe.getIdClasse());
        if (classe.getEffectifMax() == null || effectif >= classe.getEffectifMax()) {
            throw new ConflitMetierException("classe-pleine",
                    "La classe « " + classe.getNom() + " » a atteint son effectif maximal",
                    Map.of("idClasse", classe.getIdClasse(), "effectifActuel", effectif));
        }
    }
}

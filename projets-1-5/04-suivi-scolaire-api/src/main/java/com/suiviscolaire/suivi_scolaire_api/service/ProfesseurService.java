package com.suiviscolaire.suivi_scolaire_api.service;

import com.suiviscolaire.suivi_scolaire_api.dto.request.AffectationMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateProfesseurRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateProfesseurRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.response.AffectationResponse;
import com.suiviscolaire.suivi_scolaire_api.dto.response.ProfesseurResponse;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Professeur;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.ProfesseurRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD des professeurs et affectations (F4, F12).
 *
 * COUCHE       : Service
 * RÔLE         : gérer les professeurs et désigner qui enseigne quoi dans chaque classe.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : ProfesseurRepository, EnseignementRepository, MatiereRepository,
 *                ClasseRepository.
 * UTILISÉ PAR  : ProfesseurController.
 * NE FAIT PAS  : de HTTP ni de SQL direct ; la matière principale est retrouvée via
 *                enseignement (A-02).
 */
@Service
@Transactional(readOnly = true)
public class ProfesseurService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final ProfesseurRepository professeurRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final ClasseRepository classeRepository;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring injecte les dépendances par constructeur. */
    public ProfesseurService(ProfesseurRepository professeurRepository,
                             EnseignementRepository enseignementRepository,
                             MatiereRepository matiereRepository, ClasseRepository classeRepository) {
        this.professeurRepository = professeurRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.classeRepository = classeRepository;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE
    // ═════════════════════════════════════════════════════════════════════════

    /** Liste les professeurs triés par nom, avec leurs matières (A-02). */

    // ── findAll() ────────────────────────────────────────────────────────────

    /**
     * Exécute findAll().
     */

    public List<ProfesseurResponse> findAll() {
        return professeurRepository.findAllByOrderByNomAscPrenomAsc().stream()
                .map(this::toResponse).toList();
    }

    /** Consulte un professeur par identifiant (404 si inconnu). */

    // ── findById() ───────────────────────────────────────────────────────────

    /**
     * Exécute findById().
     */

    public ProfesseurResponse findById(Integer id) {
        return toResponse(required(id));
    }

    /** Crée un professeur ; l'email est unique et la matière principale doit exister. */

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Exécute create().
     */

    @Transactional
    public ProfesseurResponse create(CreateProfesseurRequest request) {
        if (professeurRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflitMetierException("professeur-email-deja-utilise",
                    "L'email « " + request.email() + " » est déjà utilisé");
        }
        matiereRepository.findById(request.idMatierePrincipale())
                .orElseThrow(() -> new RessourceIntrouvableException("Matiere", request.idMatierePrincipale()));
        return toResponse(professeurRepository.save(new Professeur(
                request.nom(), request.prenom(), request.email(), request.telephone())));
    }

    /** Modifie les coordonnées et la matière principale du professeur. */

    // ── update() ─────────────────────────────────────────────────────────────

    /**
     * Exécute update().
     */

    @Transactional
    public ProfesseurResponse update(Integer id, UpdateProfesseurRequest request) {
        Professeur professeur = required(id);
        professeurRepository.findByEmail(request.email())
                .filter(other -> !other.getIdProfesseur().equals(id))
                .ifPresent(other -> {
                    throw new ConflitMetierException("professeur-email-deja-utilise",
                            "L'email « " + request.email() + " » est déjà utilisé");
                });
        matiereRepository.findById(request.idMatierePrincipale())
                .orElseThrow(() -> new RessourceIntrouvableException("Matiere", request.idMatierePrincipale()));
        professeur.modifier(request.email(), request.telephone());
        return toResponse(professeur);
    }

    /** Refuse la suppression d'un professeur affecté à une classe (F4). */

    // ── delete() ─────────────────────────────────────────────────────────────

    /**
     * Exécute delete().
     */

    @Transactional
    public void delete(Integer id) {
        Professeur professeur = required(id);
        if (enseignementRepository.existsByIdProfesseur(id)) {
            throw new ConflitMetierException("professeur-non-supprimable",
                    "Ce professeur est encore affecté à une classe");
        }
        professeurRepository.delete(professeur);
    }

    /** Affecte une matière à un professeur dans une classe ; un seul professeur par couple (F12). */

    // ── affecter() ───────────────────────────────────────────────────────────

    /**
     * Exécute affecter().
     */

    @Transactional
    public AffectationResponse affecter(Integer id, AffectationMatiereRequest request) {
        Professeur professeur = required(id);
        Classe classe = classeRepository.findById(request.idClasse())
                .orElseThrow(() -> new RessourceIntrouvableException("Classe", request.idClasse()));
        Matiere matiere = matiereRepository.findById(request.idMatiere())
                .orElseThrow(() -> new RessourceIntrouvableException("Matiere", request.idMatiere()));
        Enseignement existant = enseignementRepository
                .findByIdClasseAndIdMatiere(request.idClasse(), request.idMatiere()).orElse(null);
        if (existant != null) {
            Professeur titulaire = professeurRepository.findById(existant.getIdProfesseur()).orElse(null);
            String nomTitulaire = titulaire == null ? "inconnu"
                    : titulaire.getPrenom() + " " + titulaire.getNom();
            throw new ConflitMetierException("enseignement-deja-affecte",
                    "La matière est déjà enseignée dans cette classe par " + nomTitulaire,
                    java.util.Map.of("professeurAffecte", nomTitulaire));
        }
        Enseignement enseignement = enseignementRepository.save(new Enseignement(
                classe.getIdClasse(), matiere.getIdMatiere(), professeur.getIdProfesseur()));
        return new AffectationResponse(enseignement.getIdEnseignement(), professeur.getIdProfesseur(),
                classe.getIdClasse(), matiere.getIdMatiere());
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne le professeur ou lève une erreur métier 404. */

    // ── required() ───────────────────────────────────────────────────────────

    /**
     * Exécute required().
     */

    public Professeur required(Integer id) {
        return professeurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Professeur", id));
    }

    /** Convertit un professeur en DTO en retrouvant ses matières via enseignement. */

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    private ProfesseurResponse toResponse(Professeur professeur) {
        List<Integer> matieres = professeur.getIdProfesseur() == null ? List.of()
                : enseignementRepository.findDistinctMatiereByIdProfesseur(professeur.getIdProfesseur());
        return new ProfesseurResponse(professeur.getIdProfesseur(), professeur.getNom(),
                professeur.getPrenom(), professeur.getEmail(), professeur.getTelephone(), matieres);
    }
}

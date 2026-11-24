package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.dto.request.CreateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleProprietaireResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ProprietaireResponse;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.Proprietaire;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireEmailDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNotFoundException;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.mapper.ProprietaireMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.ProprietaireRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des propriétaires (F1, F7).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles F1 et produire la liste enrichie des immeubles (F7).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : ProprietaireRepository, ImmeubleRepository, AppartementRepository,
 *                BailRepository, ProprietaireMapper et ImmeubleMapper.
 * UTILISÉ PAR  : ProprietaireController.
 * NE FAIT PAS  : de HTTP ni de SQL.
 */
@Service
@Transactional(readOnly = true)
public class ProprietaireService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final ProprietaireRepository proprietaireRepository;
    private final ImmeubleRepository immeubleRepository;
    private final AppartementRepository appartementRepository;
    private final BailRepository bailRepository;
    private final ProprietaireMapper proprietaireMapper;
    private final ImmeubleMapper immeubleMapper;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public ProprietaireService(ProprietaireRepository proprietaireRepository,
                               ImmeubleRepository immeubleRepository,
                               AppartementRepository appartementRepository,
                               BailRepository bailRepository,
                               ProprietaireMapper proprietaireMapper,
                               ImmeubleMapper immeubleMapper) {
        this.proprietaireRepository = proprietaireRepository;
        this.immeubleRepository = immeubleRepository;
        this.appartementRepository = appartementRepository;
        this.bailRepository = bailRepository;
        this.proprietaireMapper = proprietaireMapper;
        this.immeubleMapper = immeubleMapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F1
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ───────────────────────────────────────────────────────────

    /** Retourne les propriétaires triés par nom (F1). */
    public List<ProprietaireResponse> findAll() {
        return proprietaireRepository.findAllByOrderByNomAscPrenomAsc().stream()
                .map(proprietaireMapper::toResponse)
                .toList();
    }

    // ── findById() ──────────────────────────────────────────────────────────

    /** Retourne un propriétaire par identifiant. */
    public ProprietaireResponse findById(Integer idProprietaire) {
        return proprietaireMapper.toResponse(requireProprietaire(idProprietaire));
    }

    // ── findImmeubles() ─────────────────────────────────────────────────────

    /**
     * Retourne les immeubles d'un propriétaire avec leurs compteurs (F7).
     * Le nombre de baux en cours est calculé par jointure sur les appartements.
     *
     * @throws ProprietaireNotFoundException si le propriétaire n'existe pas
     */
    public List<ImmeubleProprietaireResponse> findImmeubles(Integer idProprietaire) {
        // ── Étape 1/3 : vérifier l'existence du propriétaire ──
        requireProprietaire(idProprietaire);

        // ── Étape 2/3 : enrichir chaque immeuble ──
        return immeubleRepository.findAllByIdProprietaireOrderByAdresseAsc(idProprietaire)
                .stream()
                .map(immeuble -> toResponse(immeuble))
                .toList();
    }

    // ── create() ────────────────────────────────────────────────────────────

    /**
     * Crée un propriétaire après contrôle de l'unicité de l'email. Modifie la base.
     *
     * @throws ProprietaireEmailDejaUtiliseException si l'email existe déjà
     */
    @Transactional
    public ProprietaireResponse create(CreateProprietaireRequest request) {
        // ── Étape 1/3 : vérifier l'unicité de l'email ──
        verifierEmailLibre(request.email(), null);

        // ── Étape 2/3 : construire et enregistrer ──
        Proprietaire proprietaire = new Proprietaire(
                request.nom().trim(), valeur(request.prenom()),
                request.email().trim(), request.telephone().trim());
        Proprietaire enregistre = proprietaireRepository.saveAndFlush(proprietaire);

        // ── Étape 3/3 : convertir en DTO ──
        return proprietaireMapper.toResponse(enregistre);
    }

    // ── update() ────────────────────────────────────────────────────────────

    /**
     * Modifie les coordonnées d'un propriétaire. Modifie la base.
     *
     * @throws ProprietaireNotFoundException si l'identifiant est inconnu
     *
     * @throws ProprietaireEmailDejaUtiliseException si l'email appartient à un autre
     */
    @Transactional
    public ProprietaireResponse update(Integer idProprietaire, UpdateProprietaireRequest request) {
        // ── Étape 1/3 : charger et contrôler l'unicité ──
        Proprietaire proprietaire = requireProprietaire(idProprietaire);
        verifierEmailLibre(request.email(), idProprietaire);

        // ── Étape 2/3 : appliquer les changements ──
        proprietaire.modifier(request.nom().trim(), valeur(request.prenom()),
                request.email().trim(), request.telephone().trim());

        // ── Étape 3/3 : convertir en DTO ──
        return proprietaireMapper.toResponse(proprietaire);
    }

    // ── delete() ────────────────────────────────────────────────────────────

    /**
     * Supprime un propriétaire sans immeuble. Modifie la base.
     *
     * @throws ProprietaireNonSupprimableException s'il possède encore des immeubles (V2)
     */
    @Transactional
    public void delete(Integer idProprietaire) {
        requireProprietaire(idProprietaire);
        if (immeubleRepository.countByIdProprietaire(idProprietaire) > 0) {
            throw new ProprietaireNonSupprimableException(idProprietaire);
        }
        proprietaireRepository.deleteById(idProprietaire);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireProprietaire() ───────────────────────────────────────────────

    /** Charge un propriétaire ou lève l'erreur métier 404. */
    private Proprietaire requireProprietaire(Integer idProprietaire) {
        return proprietaireRepository.findById(idProprietaire)
                .orElseThrow(() -> new ProprietaireNotFoundException(idProprietaire));
    }

    // ── toResponse() ────────────────────────────────────────────────────────

    /** Enrichit un immeuble avec ses deux compteurs pour F7. */
    private ImmeubleProprietaireResponse toResponse(Immeuble immeuble) {
        long nbAppartements = appartementRepository.countByIdImmeuble(immeuble.getIdImmeuble());
        long nbBauxEnCours = bailRepository.countEnCoursByIdImmeuble(
                immeuble.getIdImmeuble(), StatutBail.EN_COURS);
        return immeubleMapper.toProprietaireResponse(immeuble, nbAppartements, nbBauxEnCours);
    }

    // ── verifierEmailLibre() ────────────────────────────────────────────────

    /** Vérifie qu'aucun autre propriétaire n'utilise cet email. */
    private void verifierEmailLibre(String email, Integer idProprietaireExclu) {
        Optional<Proprietaire> existant = proprietaireRepository.findByEmail(email.trim());
        if (existant.isPresent()
                && !existant.get().getIdProprietaire().equals(idProprietaireExclu)) {
            throw new ProprietaireEmailDejaUtiliseException(email);
        }
    }

    // ── valeur() ────────────────────────────────────────────────────────────

    /** Normalise un champ facultatif : la base impose NOT NULL sur prenom. */
    private String valeur(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }
}

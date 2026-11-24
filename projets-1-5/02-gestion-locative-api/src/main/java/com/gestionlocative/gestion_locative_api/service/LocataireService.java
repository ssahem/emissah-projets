package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.dto.request.CreateLocataireRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateLocataireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.LocataireResponse;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.LocataireEmailDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.LocataireNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.LocataireNotFoundException;
import com.gestionlocative.gestion_locative_api.mapper.LocataireMapper;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des locataires (F4).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles F4, notamment l'unicité de l'email après
 *                modification et le refus de suppression d'un locataire en bail.
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : LocataireRepository, BailRepository, LocataireMapper.
 * UTILISÉ PAR  : LocataireController.
 * NE FAIT PAS  : de HTTP ni de SQL.
 */
@Service
@Transactional(readOnly = true)
public class LocataireService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final LocataireRepository locataireRepository;
    private final BailRepository bailRepository;
    private final LocataireMapper locataireMapper;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public LocataireService(LocataireRepository locataireRepository,
                            BailRepository bailRepository,
                            LocataireMapper locataireMapper) {
        this.locataireRepository = locataireRepository;
        this.bailRepository = bailRepository;
        this.locataireMapper = locataireMapper;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F4
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ───────────────────────────────────────────────────────────

    /** Retourne les locataires triés par nom (F4). */
    public List<LocataireResponse> findAll() {
        return locataireRepository.findAllByOrderByNomAscPrenomAsc().stream()
                .map(locataireMapper::toResponse)
                .toList();
    }

    // ── findById() ──────────────────────────────────────────────────────────

    /** Retourne un locataire par identifiant. */
    public LocataireResponse findById(Integer idLocataire) {
        return locataireMapper.toResponse(requireLocataire(idLocataire));
    }

    // ── create() ────────────────────────────────────────────────────────────

    /** Crée un locataire après contrôle de l'unicité de l'email. Modifie la base. */
    @Transactional
    public LocataireResponse create(CreateLocataireRequest request) {
        // ── Étape 1/3 : vérifier l'unicité de l'email ──
        verifierEmailLibre(request.email(), null);

        // ── Étape 2/3 : construire et enregistrer ──
        Locataire locataire = new Locataire(
                request.nom().trim(), valeur(request.prenom()),
                request.email().trim(), request.telephone().trim());
        Locataire enregistre = locataireRepository.saveAndFlush(locataire);

        // ── Étape 3/3 : convertir en DTO ──
        return locataireMapper.toResponse(enregistre);
    }

    // ── update() ────────────────────────────────────────────────────────────

    /** Modifie les coordonnées d'un locataire. Modifie la base. */
    @Transactional
    public LocataireResponse update(Integer idLocataire, UpdateLocataireRequest request) {
        // ── Étape 1/3 : charger et contrôler l'unicité ──
        Locataire locataire = requireLocataire(idLocataire);
        verifierEmailLibre(request.email(), idLocataire);

        // ── Étape 2/3 : appliquer les changements ──
        locataire.modifier(request.nom().trim(), valeur(request.prenom()),
                request.email().trim(), request.telephone().trim());

        // ── Étape 3/3 : convertir en DTO ──
        return locataireMapper.toResponse(locataire);
    }

    // ── delete() ────────────────────────────────────────────────────────────

    /** Supprime un locataire sans bail en cours. Modifie la base. */
    @Transactional
    public void delete(Integer idLocataire) {
        requireLocataire(idLocataire);
        if (bailRepository.countEnCoursByLocataire(idLocataire, StatutBail.EN_COURS) > 0) {
            throw new LocataireNonSupprimableException(idLocataire);
        }
        locataireRepository.deleteById(idLocataire);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireLocataire() ──────────────────────────────────────────────────

    /** Charge un locataire ou lève l'erreur métier 404. */
    private Locataire requireLocataire(Integer idLocataire) {
        return locataireRepository.findById(idLocataire)
                .orElseThrow(() -> new LocataireNotFoundException(idLocataire));
    }

    // ── verifierEmailLibre() ────────────────────────────────────────────────

    /** Vérifie qu'aucun autre locataire n'utilise cet email. */
    private void verifierEmailLibre(String email, Integer idLocataireExclu) {
        Optional<Locataire> existant = locataireRepository.findByEmail(email.trim());
        if (existant.isPresent() && !existant.get().getIdLocataire().equals(idLocataireExclu)) {
            throw new LocataireEmailDejaUtiliseException(email);
        }
    }

    // ── valeur() ────────────────────────────────────────────────────────────

    /** Normalise un champ facultatif : la base impose NOT NULL sur prenom. */
    private String valeur(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }
}

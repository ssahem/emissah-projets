package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.dto.request.CreateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RenouvellementBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.ResiliationBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RevisionLoyerRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.response.BailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PageResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.BailLocataire;
import com.gestionlocative.gestion_locative_api.entity.HistoriqueBail;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.enums.MotifHistoriqueBail;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.AppartementNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.BailChevauchementException;
import com.gestionlocative.gestion_locative_api.exception.BailDateFinInvalideException;
import com.gestionlocative.gestion_locative_api.exception.BailModificationInterditeException;
import com.gestionlocative.gestion_locative_api.exception.BailNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.BailNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.BailStatutInvalideException;
import com.gestionlocative.gestion_locative_api.exception.DateEffetInvalideException;
import com.gestionlocative.gestion_locative_api.exception.LocataireNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.RenouvellementDateInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerDateInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerDejaExistanteException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerMontantInvalideException;
import com.gestionlocative.gestion_locative_api.mapper.BailMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailLocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.HistoriqueBailRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.RevisionLoyerRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des baux (F5, F10 à F15), avec les garde-fous de F11 et F12.
 *
 * COUCHE       : Service
 * RÔLE         : vérifier la disponibilité et les dates, créer la révision initiale
 *                du loyer, exposer le locataire principal (E-08) et piloter le
 *                cycle de vie : résiliation (F13), renouvellement (F14) et
 *                révision (F15). La clôture nightly est dans BailClotureService (F16).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Bail/BailLocataire/Appartement/Locataire/RevisionLoyer/
 *                HistoriqueBail, BailMapper et l'horloge applicative.
 * UTILISÉ PAR  : BailController.
 * NE FAIT PAS  : de HTTP ni de SQL ; la contrainte d'exclusion V1 reste le filet final.
 */
@Service
@Transactional(readOnly = true)
public class BailService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    /** Taille maximale d'une page, imposée par l'énoncé (F10). */
    private static final int TAILLE_MAX = 100;

    /** Tri appliqué quand le client ne demande pas de tri explicite. */
    private static final Sort TRI_DEFAUT = Sort.by(Sort.Direction.DESC, "dateDebut");

    private final BailRepository bailRepository;
    private final BailLocataireRepository bailLocataireRepository;
    private final AppartementRepository appartementRepository;
    private final LocataireRepository locataireRepository;
    private final RevisionLoyerRepository revisionLoyerRepository;
    private final HistoriqueBailRepository historiqueBailRepository;
    private final BailMapper bailMapper;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public BailService(BailRepository bailRepository,
                       BailLocataireRepository bailLocataireRepository,
                       AppartementRepository appartementRepository,
                       LocataireRepository locataireRepository,
                       RevisionLoyerRepository revisionLoyerRepository,
                       HistoriqueBailRepository historiqueBailRepository,
                       BailMapper bailMapper,
                       Clock clock) {
        this.bailRepository = bailRepository;
        this.bailLocataireRepository = bailLocataireRepository;
        this.appartementRepository = appartementRepository;
        this.locataireRepository = locataireRepository;
        this.revisionLoyerRepository = revisionLoyerRepository;
        this.historiqueBailRepository = historiqueBailRepository;
        this.bailMapper = bailMapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F5
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ───────────────────────────────────────────────────────────

    /**
     * Retourne une page de baux, triée par date de début décroissante par défaut (F5, F10).
     * La taille demandée est plafonnée à 100 éléments.
     */
    public PageResponse<BailResponse> findAll(Pageable pageable) {
        Pageable normalise = normaliser(pageable);
        Page<Bail> page = bailRepository.findAll(normalise);
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    // ── findById() ──────────────────────────────────────────────────────────

    /** Retourne un bail par identifiant. */
    public BailResponse findById(Integer idBail) {
        return toResponse(requireBail(idBail));
    }

    // ── create() ────────────────────────────────────────────────────────────

    /**
     * Crée un bail sur un appartement libre, avec son locataire principal.
     *
     * @throws BailChevauchementException si la période est déjà occupée (F11)
     *
     * @throws BailDateFinInvalideException si la date de fin est incohérente (F12)
     */
    @Transactional
    public BailResponse create(CreateBailRequest request) {
        // ── Étape 1/5 : vérifier l'existence des références ──
        if (!appartementRepository.existsById(request.idAppartement())) {
            throw new AppartementNotFoundException(request.idAppartement());
        }
        Locataire principal = locataireRepository.findById(request.idLocataire())
                .orElseThrow(() -> new LocataireNotFoundException(request.idLocataire()));

        // ── Étape 2/5 : contrôler la période (F12) ──
        verifierDateFin(request.dateDebut(), request.dateFin());

        // ── Étape 3/5 : contrôler la disponibilité sous verrou (F11, E-09) ──
        LocalDate finRecherche = request.dateFin() == null
                ? request.dateDebut().plusDays(1) : request.dateFin();
        List<Bail> bloquants = bailRepository.findChevauchements(
                request.idAppartement(), request.dateDebut(), finRecherche);
        if (!bloquants.isEmpty()) {
            Bail bloquant = bloquants.getFirst();
            throw new BailChevauchementException(
                    bloquant.getIdBail(), bloquant.getDateDebut(), bloquant.getDateFin());
        }

        // ── Étape 4/5 : enregistrer le bail, la liaison et la révision initiale ──
        Bail bail = new Bail(
                request.dateDebut(), request.dateFin(),
                valeur(request.provisionCharges()), valeur(request.depotGarantie()),
                request.idAppartement(), request.loyerMensuel());
        Bail enregistre = bailRepository.saveAndFlush(bail);
        bailLocataireRepository.saveAndFlush(
                new BailLocataire(enregistre.getIdBail(), principal.getIdLocataire()));
        revisionLoyerRepository.saveAndFlush(new RevisionLoyer(
                enregistre.getIdBail(), enregistre.getDateDebut(), enregistre.getLoyerMensuel()));

        // ── Étape 5/5 : convertir en DTO ──
        return bailMapper.toResponse(enregistre, principal);
    }

    // ── update() ────────────────────────────────────────────────────────────

    /**
     * Modifie un bail : avant le début, loyer et date de fin ; après, date de fin seulement.
     *
     * @throws BailModificationInterditeException si le loyer est modifié après le début
     */
    @Transactional
    public BailResponse update(Integer idBail, UpdateBailRequest request) {
        // ── Étape 1/4 : charger le bail et déterminer s'il a commencé ──
        Bail bail = requireBail(idBail);
        boolean commence = !bail.getDateDebut().isAfter(LocalDate.now(clock));

        // ── Étape 2/4 : appliquer la règle du loyer ──
        if (request.loyerMensuel() != null) {
            if (commence) {
                throw new BailModificationInterditeException(idBail);
            }
            bail.modifierAvantDebut(request.loyerMensuel(), bail.getDateFin());
            revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(idBail).stream()
                    .findFirst()
                    .ifPresent(revision -> revision.modifierMontant(request.loyerMensuel()));
        }

        // ── Étape 3/4 : appliquer la date de fin, si fournie ──
        Optional<LocalDate> nouvelleFin = request.dateFin();
        if (nouvelleFin.isPresent()) {
            verifierDateFin(bail.getDateDebut(), nouvelleFin.get());
            bail.modifierDateFin(nouvelleFin.get());
        }

        // ── Étape 4/4 : convertir en DTO ──
        return toResponse(bail);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F13 à F15
    // ═════════════════════════════════════════════════════════════════════════

    // ── resilier() ──────────────────────────────────────────────────────────

    /**
     * Résilie un bail en ramenant sa fin à la date d'effet (F13).
     *
     * @throws BailStatutInvalideException si le bail n'est pas actif ou n'a pas commencé
     * @throws DateEffetInvalideException si la date est passée ou hors période
     */
    @Transactional
    public BailResponse resilier(Integer idBail, ResiliationBailRequest request) {
        // ── Étape 1/4 : charger le bail et l'horloge ──
        Bail bail = requireBail(idBail);
        LocalDate aujourdHui = LocalDate.now(clock);
        LocalDate dateEffet = request.dateEffet();

        // ── Étape 2/4 : vérifier l'état (409) ──
        if (bail.getStatut() != StatutBail.EN_COURS) {
            throw new BailStatutInvalideException(idBail, "le bail est déjà " + bail.getStatut());
        }
        if (bail.getDateDebut().isAfter(aujourdHui)) {
            throw new BailStatutInvalideException(idBail, "le bail n'a pas commencé");
        }

        // ── Étape 3/4 : vérifier la date d'effet (400) ──
        if (dateEffet.isBefore(aujourdHui) || !bail.contient(dateEffet)) {
            throw new DateEffetInvalideException(
                    idBail, dateEffet, bail.getDateDebut(), bail.getDateFin());
        }

        // ── Étape 4/4 : tracer puis appliquer la transition ──
        tracer(idBail, MotifHistoriqueBail.RESILIATION, aujourdHui,
                bail.getDateFin(), dateEffet);
        bail.resilier(dateEffet);
        return toResponse(bail);
    }

    // ── renouveler() ────────────────────────────────────────────────────────

    /**
     * Prolonge un bail en vérifiant la disponibilité de la période ajoutée (F14).
     *
     * @throws BailStatutInvalideException si le bail est résilié ou sans échéance
     * @throws RenouvellementDateInvalideException si la date n'est pas postérieure
     * @throws BailChevauchementException si la période ajoutée est occupée
     */
    @Transactional
    public BailResponse renouveler(Integer idBail, RenouvellementBailRequest request) {
        // ── Étape 1/4 : charger le bail et vérifier son état (409) ──
        Bail bail = requireBail(idBail);
        if (bail.getStatut() == StatutBail.RESILIE) {
            throw new BailStatutInvalideException(idBail, "le bail est résilié");
        }
        LocalDate finActuelle = bail.getDateFin();
        if (finActuelle == null) {
            throw new BailStatutInvalideException(idBail, "le bail n'a pas d'échéance");
        }

        // ── Étape 2/4 : vérifier la nouvelle échéance (400) ──
        LocalDate nouvelleFin = request.dateFin();
        if (!nouvelleFin.isAfter(finActuelle)) {
            throw new RenouvellementDateInvalideException(idBail, finActuelle, nouvelleFin);
        }

        // ── Étape 3/4 : contrôler la période ajoutée sous verrou (F11, E-09) ──
        // Le bail courant est exclu : son ancienne échéance borde la période ajoutée.
        List<Bail> bloquants = bailRepository.findChevauchements(
                        bail.getIdAppartement(), finActuelle, nouvelleFin).stream()
                .filter(candidat -> !candidat.getIdBail().equals(idBail))
                .toList();
        if (!bloquants.isEmpty()) {
            Bail bloquant = bloquants.getFirst();
            throw new BailChevauchementException(
                    bloquant.getIdBail(), bloquant.getDateDebut(), bloquant.getDateFin());
        }

        // ── Étape 4/4 : tracer puis appliquer la transition ──
        LocalDate aujourdHui = LocalDate.now(clock);
        tracer(idBail, MotifHistoriqueBail.RENOUVELLEMENT, aujourdHui, finActuelle, nouvelleFin);
        bail.renouveler(nouvelleFin);
        return toResponse(bail);
    }

    // ── reviserLoyer() ──────────────────────────────────────────────────────

    /**
     * Ajoute une révision de loyer et met à jour le montant courant (F15).
     *
     * @throws RevisionLoyerMontantInvalideException si le montant n'est pas positif
     * @throws RevisionLoyerDejaExistanteException si la date est déjà utilisée
     * @throws RevisionLoyerDateInvalideException si la date est hors période ou antérieure
     */
    @Transactional
    public BailResponse reviserLoyer(Integer idBail, RevisionLoyerRequest request) {
        // ── Étape 1/4 : vérifier le montant (400) ──
        BigDecimal montant = request.montant();
        if (montant == null || montant.signum() <= 0) {
            throw new RevisionLoyerMontantInvalideException(montant);
        }

        // ── Étape 2/4 : charger le bail et détecter une date déjà utilisée (409) ──
        Bail bail = requireBail(idBail);
        LocalDate dateEffet = request.dateEffet();
        if (revisionLoyerRepository.existsByIdBailAndDateEffet(idBail, dateEffet)) {
            throw new RevisionLoyerDejaExistanteException(idBail, dateEffet);
        }

        // ── Étape 3/4 : vérifier la période et la dernière révision (400) ──
        LocalDate derniere = revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(idBail)
                .stream()
                .findFirst()
                .map(RevisionLoyer::getDateEffet)
                .orElse(null);
        if (!bail.contient(dateEffet) || (derniere != null && !dateEffet.isAfter(derniere))) {
            throw new RevisionLoyerDateInvalideException(
                    idBail, dateEffet, derniere, bail.getDateDebut(), bail.getDateFin());
        }

        // ── Étape 4/4 : écrire la révision puis le loyer courant ──
        revisionLoyerRepository.saveAndFlush(new RevisionLoyer(idBail, dateEffet, montant));
        bail.appliquerLoyer(montant);
        return toResponse(bail);
    }

    // ── delete() ────────────────────────────────────────────────────────────

    /** Supprime un bail qui n'a pas commencé. Modifie la base. */
    @Transactional
    public void delete(Integer idBail) {
        Bail bail = requireBail(idBail);
        if (!bail.getDateDebut().isAfter(LocalDate.now(clock))) {
            throw new BailNonSupprimableException(idBail);
        }
        bailLocataireRepository.deleteByIdIdBail(idBail);
        revisionLoyerRepository.deleteByIdBail(idBail);
        historiqueBailRepository.deleteByIdBail(idBail);
        bailRepository.delete(bail);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireBail() ───────────────────────────────────────────────────────

    /** Charge un bail ou lève l'erreur métier 404. */
    private Bail requireBail(Integer idBail) {
        return bailRepository.findById(idBail)
                .orElseThrow(() -> new BailNotFoundException(idBail));
    }

    // ── toResponse() ────────────────────────────────────────────────────────

    /** Enrichit le bail avec son locataire principal avant conversion. */
    private BailResponse toResponse(Bail bail) {
        Locataire principal = bailLocataireRepository.findByIdIdBail(bail.getIdBail()).stream()
                .findFirst()
                .map(liaison -> locataireRepository.findById(liaison.getId().getIdLocataire())
                        .orElse(null))
                .orElse(null);
        return bailMapper.toResponse(bail, principal);
    }

    // ── tracer() ────────────────────────────────────────────────────────────

    /** Écrit un événement de cycle de vie dans historique_bail (V5). */
    private void tracer(Integer idBail, MotifHistoriqueBail motif, LocalDate dateEvenement,
                        LocalDate dateFinAvant, LocalDate dateFinApres) {
        historiqueBailRepository.saveAndFlush(new HistoriqueBail(
                idBail, motif, dateEvenement, dateFinAvant, dateFinApres));
    }

    // ── verifierDateFin() ───────────────────────────────────────────────────

    /** Vérifie qu'une date de fin, si elle existe, est au moins un mois après le début (F12). */
    private void verifierDateFin(LocalDate dateDebut, LocalDate dateFin) {
        if (dateFin != null
                && (!dateFin.isAfter(dateDebut) || dateFin.isBefore(dateDebut.plusMonths(1)))) {
            throw new BailDateFinInvalideException(dateDebut, dateFin);
        }
    }

    // ── valeur() ────────────────────────────────────────────────────────────

    /** Applique zéro pour un montant facultatif non renseigné (NOT NULL en base). */
    private BigDecimal valeur(BigDecimal montant) {
        return montant == null ? BigDecimal.ZERO : montant;
    }

    // ── normaliser() ────────────────────────────────────────────────────────

    /** Plafonne la taille de page et applique le tri décroissant par défaut (F10). */
    private Pageable normaliser(Pageable pageable) {
        int taille = Math.min(pageable.getPageSize(), TAILLE_MAX);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : TRI_DEFAUT;
        return PageRequest.of(pageable.getPageNumber(), taille, sort);
    }
}

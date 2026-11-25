package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.dto.request.CreateAppartementRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateAppartementRequest;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementLibreResponse;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementResponse;
import com.gestionlocative.gestion_locative_api.dto.response.HistoriqueBailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleAppartementsLibresResponse;
import com.gestionlocative.gestion_locative_api.dto.response.LoyerHistoriqueResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PeriodeOccupationResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.exception.AppartementEtageHorsImmeubleException;
import com.gestionlocative.gestion_locative_api.exception.AppartementNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.AppartementNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.AppartementNumeroDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.ImmeubleNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.SurfaceRechercheInvalideException;
import com.gestionlocative.gestion_locative_api.mapper.AppartementMapper;
import com.gestionlocative.gestion_locative_api.mapper.BailMapper;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailLocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.RevisionLoyerRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des appartements (F3, F8, F9, F17, F18).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles F3, produire l'historique des baux et
 *                rechercher des appartements par critères ; restituer les
 *                disponibilités (F17) et l'occupation reconstituée (F18).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : repositories Appartement/Immeuble/Bail/BailLocataire/Locataire/
 *                RevisionLoyer, les mappers Appartement/Bail/Immeuble et l'horloge.
 * UTILISÉ PAR  : AppartementController.
 * NE FAIT PAS  : de HTTP ni de SQL.
 */
@Service
@Transactional(readOnly = true)
public class AppartementService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final AppartementRepository appartementRepository;
    private final ImmeubleRepository immeubleRepository;
    private final BailRepository bailRepository;
    private final BailLocataireRepository bailLocataireRepository;
    private final LocataireRepository locataireRepository;
    private final RevisionLoyerRepository revisionLoyerRepository;
    private final AppartementMapper appartementMapper;
    private final BailMapper bailMapper;
    private final ImmeubleMapper immeubleMapper;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public AppartementService(AppartementRepository appartementRepository,
                              ImmeubleRepository immeubleRepository,
                              BailRepository bailRepository,
                              BailLocataireRepository bailLocataireRepository,
                              LocataireRepository locataireRepository,
                              RevisionLoyerRepository revisionLoyerRepository,
                              AppartementMapper appartementMapper,
                              BailMapper bailMapper,
                              ImmeubleMapper immeubleMapper,
                              Clock clock) {
        this.appartementRepository = appartementRepository;
        this.immeubleRepository = immeubleRepository;
        this.bailRepository = bailRepository;
        this.bailLocataireRepository = bailLocataireRepository;
        this.locataireRepository = locataireRepository;
        this.revisionLoyerRepository = revisionLoyerRepository;
        this.appartementMapper = appartementMapper;
        this.bailMapper = bailMapper;
        this.immeubleMapper = immeubleMapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F3
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ────────────────────────────────────────────────────────────

    /** Retourne les appartements triés par étage puis surface (F3). */
    public List<AppartementResponse> findAll() {
        return appartementRepository.findAllByOrderByEtageAscSurfaceAsc().stream()
                .map(appartementMapper::toResponse)
                .toList();
    }

    // ── findById() ───────────────────────────────────────────────────────────

    /** Retourne un appartement par identifiant. */
    public AppartementResponse findById(Integer idAppartement) {
        return appartementMapper.toResponse(requireAppartement(idAppartement));
    }

    // ── findBaux() ───────────────────────────────────────────────────────────

    /**
     * Retourne l'historique des baux d'un appartement (F8), du plus récent au plus ancien.
     * Le locataire principal est résolu via la table de liaison (E-08) et les montants
     * antérieurs proviennent de revision_loyer (V6).
     *
     * @throws AppartementNotFoundException si l'appartement n'existe pas
     */
    public List<HistoriqueBailResponse> findBaux(Integer idAppartement) {
        // ── Étape 1/3 : vérifier l'existence de l'appartement ────────────────
        requireAppartement(idAppartement);

        // ── Étape 2/3 : enrichir chaque bail ─────────────────────────────────
        LocalDate aujourdHui = LocalDate.now(clock);
        return bailRepository.findAllByIdAppartementOrderByDateDebutDesc(idAppartement).stream()
                .map(bail -> toHistoriqueResponse(bail, aujourdHui))
                .toList();
    }

    // ── search() ─────────────────────────────────────────────────────────────

    /**
     * Recherche les appartements selon les critères fournis (F9).
     * Sans critère, le tri reste identique à celui de la liste complète.
     *
     * @throws SurfaceRechercheInvalideException si surfaceMin dépasse surfaceMax
     */
    public List<AppartementResponse> search(BigDecimal surfaceMin, BigDecimal surfaceMax,
                                            Integer nbPieces) {
        // ── Étape 1/2 : refuser des bornes qui ne peuvent rien matcher ───────
        if (surfaceMin != null && surfaceMax != null && surfaceMin.compareTo(surfaceMax) > 0) {
            throw new SurfaceRechercheInvalideException(surfaceMin, surfaceMax);
        }

        // ── Étape 2/2 : déléguer la requête dynamique au repository ──────────
        return appartementRepository.search(surfaceMin, surfaceMax, nbPieces).stream()
                .map(appartementMapper::toResponse)
                .toList();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RESTITUTION — F17 ET F18
    // ═════════════════════════════════════════════════════════════════════════

    // ── findLibres() ─────────────────────────────────────────────────────────

    /**
     * Liste les appartements libres à une date, groupés par immeuble (F17).
     * Une date nulle signifie « aujourd'hui » ; un bail résilié ou clôturé ne
     * rend pas l'appartement occupé. Le dernier loyer connu vient de
     * revision_loyer (V6), avec repli sur le loyer courant du bail.
     */
    public List<ImmeubleAppartementsLibresResponse> findLibres(LocalDate date) {
        // ── Étape 1/3 : résoudre la date et les appartements occupés ─────────
        LocalDate dateRecherche = date != null ? date : LocalDate.now(clock);
        Set<Integer> occupes = new HashSet<>(
                bailRepository.findIdAppartementsOccupesLe(dateRecherche));

        // ── Étape 2/3 : regrouper les appartements disponibles par immeuble ──
        Map<Integer, List<AppartementLibreResponse>> parImmeuble = appartementRepository
                .findAllByOrderByEtageAscSurfaceAsc().stream()
                .filter(appartement -> !occupes.contains(appartement.getIdAppartement()))
                .collect(Collectors.groupingBy(
                        Appartement::getIdImmeuble,
                        LinkedHashMap::new,
                        Collectors.mapping(appartement -> appartementMapper.toLibreResponse(
                                        appartement, dernierLoyerConnu(appartement.getIdAppartement())),
                                Collectors.toList())));

        // ── Étape 3/3 : joindre les immeubles concernés, triés par adresse ───
        return immeubleRepository.findAllById(parImmeuble.keySet()).stream()
                .sorted(Comparator.comparing(Immeuble::getAdresse))
                .map(immeuble -> immeubleMapper.toLibresResponse(
                        immeuble, parImmeuble.get(immeuble.getIdImmeuble())))
                .toList();
    }

    // ── findOccupation() ─────────────────────────────────────────────────────

    /**
     * Reconstitue l'occupation d'un appartement jusqu'à aujourd'hui (F18).
     * Chaque bail est découpé selon ses révisions de loyer ; les intervalles
     * sans bail produisent des périodes VACANT explicites.
     *
     * @throws AppartementNotFoundException si l'appartement n'existe pas
     */
    public List<PeriodeOccupationResponse> findOccupation(Integer idAppartement) {
        // ── Étape 1/4 : vérifier l'appartement et la période à couvrir ───────
        Appartement appartement = requireAppartement(idAppartement);
        LocalDate aujourdHui = LocalDate.now(clock);
        LocalDate dateService = appartement.getDateCreation() == null
                || appartement.getDateCreation().isAfter(aujourdHui)
                ? aujourdHui : appartement.getDateCreation();
        List<Bail> baux = bailRepository.findAllByIdAppartementOrderByDateDebutAsc(idAppartement)
                .stream()
                .filter(bail -> !bail.getDateDebut().isAfter(aujourdHui))
                .toList();

        // ── Étape 2/4 : un appartement jamais loué n'a qu'une vacance ouver… ─
        if (baux.isEmpty()) {
            return List.of(periodeVacante(dateService, null));
        }

        // ── Étape 3/4 : alterner vacances et baux, du plus ancien à aujourd… ─
        List<PeriodeOccupationResponse> periodes = new ArrayList<>();
        LocalDate curseur = dateService;
        for (Bail bail : baux) {
            LocalDate debut = bail.getDateDebut().isBefore(dateService)
                    ? dateService : bail.getDateDebut();
            LocalDate fin = bail.getDateFin() == null || bail.getDateFin().isAfter(aujourdHui)
                    ? aujourdHui : bail.getDateFin();
            if (debut.isAfter(curseur)) {
                periodes.add(periodeVacante(curseur, debut.minusDays(1)));
            }
            if (!fin.isBefore(debut)) {
                periodes.addAll(periodesLouees(bail, debut, fin));
                curseur = fin.plusDays(1);
            }
        }

        // ── Étape 4/4 : clore par la vacance courante si elle existe ─────────
        if (!curseur.isAfter(aujourdHui)) {
            periodes.add(periodeVacante(curseur, aujourdHui));
        }
        return periodes;
    }

    // ── create() ─────────────────────────────────────────────────────────────

    /**
     * Crée un appartement après vérification de l'immeuble, de l'étage et du numéro.
     *
     * @throws ImmeubleNotFoundException si l'immeuble n'existe pas
     *
     * @throws AppartementEtageHorsImmeubleException si l'étage dépasse le nombre d'étages
     *
     * @throws AppartementNumeroDejaUtiliseException si le numéro existe déjà dans l'immeuble
     */
    @Transactional
    public AppartementResponse create(CreateAppartementRequest request) {
        // ── Étape 1/3 : charger l'immeuble et contrôler l'étage ──────────────
        Immeuble immeuble = immeubleRepository.findById(request.idImmeuble())
                .orElseThrow(() -> new ImmeubleNotFoundException(request.idImmeuble()));
        if (request.etage() > immeuble.getNbEtages()) {
            throw new AppartementEtageHorsImmeubleException(request.etage(), immeuble.getNbEtages());
        }

        // ── Étape 2/3 : contrôler l'unicité du numéro puis enregistrer ───────
        if (appartementRepository.findByNumeroAndIdImmeuble(
                request.numero().trim(), request.idImmeuble()).isPresent()) {
            throw new AppartementNumeroDejaUtiliseException(request.numero(), request.idImmeuble());
        }
        Appartement appartement = new Appartement(
                request.numero().trim(), request.surface(), request.nbPieces(),
                request.etage(), request.idImmeuble(), LocalDate.now(clock));
        Appartement enregistre = appartementRepository.saveAndFlush(appartement);

        // ── Étape 3/3 : convertir en DTO ─────────────────────────────────────
        return appartementMapper.toResponse(enregistre);
    }

    // ── update() ─────────────────────────────────────────────────────────────

    /** Modifie surface et nombre de pièces ; l'immeuble reste figé (F3). */
    @Transactional
    public AppartementResponse update(Integer idAppartement, UpdateAppartementRequest request) {
        // ── Étape 1/3 : charger l'appartement ────────────────────────────────
        Appartement appartement = requireAppartement(idAppartement);

        // ── Étape 2/3 : appliquer les changements ────────────────────────────
        appartement.modifier(request.surface(), request.nbPieces());

        // ── Étape 3/3 : convertir en DTO ─────────────────────────────────────
        return appartementMapper.toResponse(appartement);
    }

    // ── delete() ─────────────────────────────────────────────────────────────

    /** Supprime un appartement jamais loué. Modifie la base. */
    @Transactional
    public void delete(Integer idAppartement) {
        requireAppartement(idAppartement);
        if (bailRepository.countByIdAppartement(idAppartement) > 0) {
            throw new AppartementNonSupprimableException(idAppartement);
        }
        appartementRepository.deleteById(idAppartement);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireAppartement() ─────────────────────────────────────────────────

    /** Charge un appartement ou lève l'erreur métier 404. */
    private Appartement requireAppartement(Integer idAppartement) {
        return appartementRepository.findById(idAppartement)
                .orElseThrow(() -> new AppartementNotFoundException(idAppartement));
    }

    // ── toHistoriqueResponse() ───────────────────────────────────────────────

    /** Enrichit un bail avec son locataire principal et ses loyers passés (F8). */
    private HistoriqueBailResponse toHistoriqueResponse(Bail bail, LocalDate date) {
        List<LoyerHistoriqueResponse> loyersPasses = revisionLoyerRepository
                .findByIdBailOrderByDateEffetDesc(bail.getIdBail()).stream()
                .skip(1)
                .map(revision -> new LoyerHistoriqueResponse(
                        revision.getDateEffet(), revision.getMontant()))
                .toList();
        return bailMapper.toHistoriqueResponse(
                bail, principal(bail.getIdBail()), loyersPasses, bail.estEnCoursLe(date));
    }

    // ── principal() ──────────────────────────────────────────────────────────

    /** Résout le locataire principal d'un bail via la table de liaison (E-08). */
    private Locataire principal(Integer idBail) {
        return bailLocataireRepository.findByIdIdBail(idBail).stream()
                .findFirst()
                .map(liaison -> locataireRepository.findById(liaison.getId().getIdLocataire())
                        .orElse(null))
                .orElse(null);
    }

    // ── dernierLoyerConnu() ──────────────────────────────────────────────────

    /** Retourne le dernier montant historisé du bail le plus récent (F17). */
    private BigDecimal dernierLoyerConnu(Integer idAppartement) {
        return bailRepository.findAllByIdAppartementOrderByDateDebutDesc(idAppartement).stream()
                .findFirst()
                .map(bail -> revisionLoyerRepository
                        .findByIdBailOrderByDateEffetDesc(bail.getIdBail()).stream()
                        .findFirst()
                        .map(RevisionLoyer::getMontant)
                        .orElse(bail.getLoyerMensuel()))
                .orElse(null);
    }

    // ── periodeVacante() ─────────────────────────────────────────────────────

    /** Construit une période de vacance, sans bail ni locataire (F18). */
    private PeriodeOccupationResponse periodeVacante(LocalDate debut, LocalDate fin) {
        return new PeriodeOccupationResponse(debut, fin, "VACANT", null, null, null, null);
    }

    // ── periodesLouees() ─────────────────────────────────────────────────────

    /** Découpe un bail en paliers de loyer selon revision_loyer (F18). */
    private List<PeriodeOccupationResponse> periodesLouees(Bail bail, LocalDate debut, LocalDate fin) {
        Locataire locataire = principal(bail.getIdBail());
        List<RevisionLoyer> revisions = revisionLoyerRepository
                .findByIdBailOrderByDateEffetAsc(bail.getIdBail()).stream()
                .filter(revision -> !revision.getDateEffet().isAfter(fin))
                .toList();
        List<PeriodeOccupationResponse> resultat = new ArrayList<>();
        BigDecimal montantCourant = revisions.isEmpty()
                ? bail.getLoyerMensuel() : revisions.getFirst().getMontant();
        LocalDate curseur = debut;

        for (int i = 0; i < revisions.size(); i++) {
            RevisionLoyer revision = revisions.get(i);
            if (revision.getDateEffet().isAfter(curseur)) {
                resultat.add(periodeLouee(bail, locataire, curseur,
                        revision.getDateEffet().minusDays(1), montantCourant));
                curseur = revision.getDateEffet();
            }
            LocalDate finPalier = i + 1 < revisions.size()
                    ? revisions.get(i + 1).getDateEffet().minusDays(1)
                    : fin;
            if (finPalier.isAfter(fin)) {
                finPalier = fin;
            }
            if (finPalier.isBefore(curseur)) {
                continue;
            }
            resultat.add(periodeLouee(bail, locataire, curseur, finPalier, revision.getMontant()));
            montantCourant = revision.getMontant();
            curseur = finPalier.plusDays(1);
        }

        if (!curseur.isAfter(fin)) {
            resultat.add(periodeLouee(bail, locataire, curseur, fin, montantCourant));
        }
        return resultat;
    }

    // ── periodeLouee() ───────────────────────────────────────────────────────

    /** Construit une période louée avec son locataire et son loyer applicable. */
    private PeriodeOccupationResponse periodeLouee(
            Bail bail, Locataire locataire, LocalDate debut, LocalDate fin, BigDecimal loyer) {
        return new PeriodeOccupationResponse(
                debut, fin, "LOUE", bail.getIdBail(),
                locataire == null ? null : locataire.getNom(),
                locataire == null ? null : locataire.getPrenom(),
                loyer);
    }
}

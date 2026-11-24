package com.gestionlocative.gestion_locative_api.service;

import com.gestionlocative.gestion_locative_api.dto.request.CreateImmeubleRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateImmeubleRequest;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementOccupationResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleResponse;
import com.gestionlocative.gestion_locative_api.dto.response.RevenuAppartementResponse;
import com.gestionlocative.gestion_locative_api.dto.response.RevenuImmeubleResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.exception.ImmeubleNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.ImmeubleNotFoundException;
import com.gestionlocative.gestion_locative_api.exception.MoisInvalideException;
import com.gestionlocative.gestion_locative_api.exception.NbEtagesInsuffisantException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNotFoundException;
import com.gestionlocative.gestion_locative_api.mapper.AppartementMapper;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.ProprietaireRepository;
import com.gestionlocative.gestion_locative_api.repository.RevisionLoyerRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier des immeubles (F2, F6, F19).
 *
 * COUCHE       : Service
 * RÔLE         : appliquer les règles F2, produire le nombre d'appartements,
 *                calculer l'état d'occupation (F6) et le revenu locatif (F19).
 * TRANSACTION  : lecture seule par défaut ; les écritures portent @Transactional.
 * DÉPEND DE    : ImmeubleRepository, ProprietaireRepository, AppartementRepository,
 *                BailRepository, RevisionLoyerRepository, les mappers et l'horloge.
 * UTILISÉ PAR  : ImmeubleController.
 * NE FAIT PAS  : de HTTP ni de SQL.
 */
@Service
@Transactional(readOnly = true)
public class ImmeubleService {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final ImmeubleRepository immeubleRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final AppartementRepository appartementRepository;
    private final BailRepository bailRepository;
    private final RevisionLoyerRepository revisionLoyerRepository;
    private final ImmeubleMapper immeubleMapper;
    private final AppartementMapper appartementMapper;
    private final Clock clock;

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ═════════════════════════════════════════════════════════════════════════

    /** Spring fournit les dépendances ; l'injection par constructeur facilite les tests. */
    public ImmeubleService(ImmeubleRepository immeubleRepository,
                           ProprietaireRepository proprietaireRepository,
                           AppartementRepository appartementRepository,
                           BailRepository bailRepository,
                           RevisionLoyerRepository revisionLoyerRepository,
                           ImmeubleMapper immeubleMapper,
                           AppartementMapper appartementMapper,
                           Clock clock) {
        this.immeubleRepository = immeubleRepository;
        this.proprietaireRepository = proprietaireRepository;
        this.appartementRepository = appartementRepository;
        this.bailRepository = bailRepository;
        this.revisionLoyerRepository = revisionLoyerRepository;
        this.immeubleMapper = immeubleMapper;
        this.appartementMapper = appartementMapper;
        this.clock = clock;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CAS D'USAGE — F2
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAll() ───────────────────────────────────────────────────────────

    /** Retourne les immeubles triés par adresse, avec leur nombre d'appartements. */
    public List<ImmeubleResponse> findAll() {
        return immeubleRepository.findAllByOrderByAdresseAsc().stream()
                .map(immeuble -> immeubleMapper.toResponse(
                        immeuble, appartementRepository.countByIdImmeuble(immeuble.getIdImmeuble())))
                .toList();
    }

    // ── findById() ──────────────────────────────────────────────────────────

    /** Retourne un immeuble par identifiant. */
    public ImmeubleResponse findById(Integer idImmeuble) {
        Immeuble immeuble = requireImmeuble(idImmeuble);
        return immeubleMapper.toResponse(
                immeuble, appartementRepository.countByIdImmeuble(idImmeuble));
    }

    // ── findAppartements() ──────────────────────────────────────────────────

    /**
     * Retourne les appartements d'un immeuble avec leur état d'occupation (F6).
     * L'état est calculé à la date courante : LOUE si un bail en cours couvre
     * aujourd'hui, A_VENIR si un bail est prévu, LIBRE sinon.
     *
     * @throws ImmeubleNotFoundException si l'immeuble n'existe pas
     */
    public List<AppartementOccupationResponse> findAppartements(Integer idImmeuble) {
        // ── Étape 1/3 : vérifier l'existence de l'immeuble ──
        requireImmeuble(idImmeuble);

        // ── Étape 2/3 : calculer l'occupation à la date courante ──
        LocalDate aujourdHui = LocalDate.now(clock);
        return appartementRepository.findAllByIdImmeubleOrderByEtageAscSurfaceAsc(idImmeuble)
                .stream()
                .map(appartement -> toOccupationResponse(appartement, aujourdHui))
                .toList();
    }

    // ── calculerRevenu() ────────────────────────────────────────────────────

    /**
     * Calcule le revenu locatif d'un immeuble sur un mois (F19).
     * Les loyers sont pondérés par le nombre de jours couverts dans le mois,
     * en tenant compte des révisions de revision_loyer (E-06).
     *
     * @throws MoisInvalideException si le mois n'est pas au format AAAA-MM
     * @throws ImmeubleNotFoundException si l'immeuble n'existe pas
     */
    public RevenuImmeubleResponse calculerRevenu(Integer idImmeuble, String mois) {
        // ── Étape 1/4 : vérifier l'immeuble et le format du mois ──
        requireImmeuble(idImmeuble);
        YearMonth periode = parseMois(mois);
        LocalDate debutMois = periode.atDay(1);
        LocalDate finMois = periode.atEndOfMonth();
        int nbJours = periode.lengthOfMonth();

        // ── Étape 2/4 : charger les baux actifs sur le mois, groupés par logement ──
        List<Appartement> appartements = appartementRepository
                .findAllByIdImmeubleOrderByEtageAscSurfaceAsc(idImmeuble);
        Map<Integer, List<Bail>> bauxParAppartement = bailRepository
                .findActifsByIdImmeubleEntre(idImmeuble, debutMois, finMois).stream()
                .collect(Collectors.groupingBy(Bail::getIdAppartement));

        // ── Étape 3/4 : calculer le revenu et l'occupation de chaque logement ──
        List<RevenuAppartementResponse> detail = new ArrayList<>();
        long totalJoursOccupes = 0;
        for (Appartement appartement : appartements) {
            BigDecimal cumulPondere = BigDecimal.ZERO;
            long joursOccupes = 0;
            for (Bail bail : bauxParAppartement.getOrDefault(
                    appartement.getIdAppartement(), List.of())) {
                LocalDate debut = bail.getDateDebut().isAfter(debutMois)
                        ? bail.getDateDebut() : debutMois;
                LocalDate fin = bail.getDateFin() == null || bail.getDateFin().isAfter(finMois)
                        ? finMois : bail.getDateFin();
                if (fin.isBefore(debut)) {
                    continue;
                }
                joursOccupes += ChronoUnit.DAYS.between(debut, fin) + 1;
                cumulPondere = cumulPondere.add(loyersPonderes(bail, debut, fin));
            }
            totalJoursOccupes += joursOccupes;
            detail.add(new RevenuAppartementResponse(
                    appartement.getIdAppartement(), appartement.getNumero(),
                    cumulPondere.divide(BigDecimal.valueOf(nbJours), 2, RoundingMode.HALF_UP),
                    joursOccupes,
                    pourcentage(joursOccupes, nbJours)));
        }

        // ── Étape 4/4 : consolider les totaux ──
        BigDecimal revenuTotal = detail.stream()
                .map(RevenuAppartementResponse::revenu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long capacite = (long) appartements.size() * nbJours;
        BigDecimal tauxOccupation = capacite == 0
                ? BigDecimal.ZERO : pourcentage(totalJoursOccupes, capacite);
        return new RevenuImmeubleResponse(
                idImmeuble, periode.toString(), revenuTotal, tauxOccupation, detail);
    }

    // ── create() ────────────────────────────────────────────────────────────

    /**
     * Crée un immeuble après vérification du propriétaire. Modifie la base.
     *
     * @throws ProprietaireNotFoundException si le propriétaire n'existe pas
     */
    @Transactional
    public ImmeubleResponse create(CreateImmeubleRequest request) {
        // ── Étape 1/3 : vérifier que le propriétaire existe ──
        if (!proprietaireRepository.existsById(request.idProprietaire())) {
            throw new ProprietaireNotFoundException(request.idProprietaire());
        }

        // ── Étape 2/3 : construire et enregistrer ──
        Immeuble immeuble = new Immeuble(
                request.adresse().trim(), request.codePostal().trim(), request.ville().trim(),
                request.idProprietaire(), request.nbEtages());
        Immeuble enregistre = immeubleRepository.saveAndFlush(immeuble);

        // ── Étape 3/3 : convertir en DTO ──
        return immeubleMapper.toResponse(enregistre, 0L);
    }

    // ── update() ────────────────────────────────────────────────────────────

    /**
     * Modifie un immeuble en vérifiant le mandat et l'étage maximal. Modifie la base.
     *
     * @throws NbEtagesInsuffisantException si le nouveau nombre d'étages est trop bas
     */
    @Transactional
    public ImmeubleResponse update(Integer idImmeuble, UpdateImmeubleRequest request) {
        // ── Étape 1/3 : charger, vérifier le propriétaire et l'étage maximal ──
        Immeuble immeuble = requireImmeuble(idImmeuble);
        if (!proprietaireRepository.existsById(request.idProprietaire())) {
            throw new ProprietaireNotFoundException(request.idProprietaire());
        }
        Integer etageMax = appartementRepository.findMaxEtageByIdImmeuble(idImmeuble);
        if (etageMax != null && request.nbEtages() < etageMax) {
            throw new NbEtagesInsuffisantException(idImmeuble, etageMax);
        }

        // ── Étape 2/3 : appliquer les changements ──
        immeuble.modifier(request.adresse().trim(), request.codePostal().trim(),
                request.ville().trim(), request.idProprietaire(), request.nbEtages());

        // ── Étape 3/3 : convertir en DTO ──
        return immeubleMapper.toResponse(
                immeuble, appartementRepository.countByIdImmeuble(idImmeuble));
    }

    // ── delete() ────────────────────────────────────────────────────────────

    /** Supprime un immeuble vide. Modifie la base. */
    @Transactional
    public void delete(Integer idImmeuble) {
        requireImmeuble(idImmeuble);
        if (appartementRepository.countByIdImmeuble(idImmeuble) > 0) {
            throw new ImmeubleNonSupprimableException(idImmeuble);
        }
        immeubleRepository.deleteById(idImmeuble);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  OUTILS INTERNES
    // ═════════════════════════════════════════════════════════════════════════

    // ── requireImmeuble() ───────────────────────────────────────────────────

    /** Charge un immeuble ou lève l'erreur métier 404. */
    private Immeuble requireImmeuble(Integer idImmeuble) {
        return immeubleRepository.findById(idImmeuble)
                .orElseThrow(() -> new ImmeubleNotFoundException(idImmeuble));
    }

    // ── toOccupationResponse() ──────────────────────────────────────────────

    /** Détermine l'état d'occupation d'un appartement à une date donnée (F6). */
    private AppartementOccupationResponse toOccupationResponse(
            Appartement appartement, LocalDate date) {
        List<Bail> baux = bailRepository.findAllByIdAppartementOrderByDateDebutDesc(
                appartement.getIdAppartement());
        boolean occupe = baux.stream().anyMatch(bail -> bail.estEnCoursLe(date));
        String statut;
        if (occupe) {
            statut = "LOUE";
        } else if (baux.stream().anyMatch(bail -> bail.estFuturLe(date))) {
            statut = "A_VENIR";
        } else {
            statut = "LIBRE";
        }
        return appartementMapper.toOccupationResponse(appartement, occupe, statut);
    }

    // ── parseMois() ─────────────────────────────────────────────────────────

    /** Convertit un mois AAAA-MM strictement, sans laisser java.time le corriger. */
    private YearMonth parseMois(String mois) {
        if (mois == null || !mois.matches("\\d{4}-\\d{2}")) {
            throw new MoisInvalideException(mois);
        }
        try {
            return YearMonth.parse(mois,
                    DateTimeFormatter.ofPattern("uuuu-MM").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ex) {
            throw new MoisInvalideException(mois);
        }
    }

    // ── loyersPonderes() ────────────────────────────────────────────────────

    /**
     * Somme des loyers multipliés par leur nombre de jours, sur la période demandée.
     * La division par le nombre de jours du mois est faite ensuite par l'appelant,
     * ce qui évite les arrondis intermédiaires (F19).
     */
    private BigDecimal loyersPonderes(Bail bail, LocalDate debut, LocalDate fin) {
        List<RevisionLoyer> revisions = revisionLoyerRepository
                .findByIdBailOrderByDateEffetAsc(bail.getIdBail());
        BigDecimal cumul = BigDecimal.ZERO;
        BigDecimal montantCourant = revisions.isEmpty()
                ? bail.getLoyerMensuel() : revisions.getFirst().getMontant();
        LocalDate curseur = debut;

        for (int i = 0; i < revisions.size(); i++) {
            RevisionLoyer revision = revisions.get(i);
            if (revision.getDateEffet().isAfter(fin)) {
                break;
            }
            if (revision.getDateEffet().isAfter(curseur)) {
                cumul = cumul.add(ponderer(montantCourant, curseur,
                        revision.getDateEffet().minusDays(1)));
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
            cumul = cumul.add(ponderer(revision.getMontant(), curseur, finPalier));
            montantCourant = revision.getMontant();
            curseur = finPalier.plusDays(1);
        }

        if (!curseur.isAfter(fin)) {
            cumul = cumul.add(ponderer(montantCourant, curseur, fin));
        }
        return cumul;
    }

    // ── ponderer() ──────────────────────────────────────────────────────────

    /** Multiplie un montant par le nombre de jours de la période inclusive. */
    private BigDecimal ponderer(BigDecimal montant, LocalDate debut, LocalDate fin) {
        long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
        return montant.multiply(BigDecimal.valueOf(jours));
    }

    // ── pourcentage() ───────────────────────────────────────────────────────

    /** Retourne un pourcentage arrondi à deux décimales. */
    private BigDecimal pourcentage(long joursOccupes, long capacite) {
        return BigDecimal.valueOf(joursOccupes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacite), 2, RoundingMode.HALF_UP);
    }
}

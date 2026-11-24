package com.interventionsterrain.interventions_terrain_api.service;

import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceDisponibleResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.DisponiblesResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxReussiteResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxTypeResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienDisponibleResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TypeInterventionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Restitution F17 à F19 : disponibilité, charge d'agence et taux de réussite. */
@Service
@Transactional(readOnly = true)
public class RestitutionService {

    private static final String RAISON_AUCUN = "Aucun technicien n'est à la fois disponible sur le "
            + "créneau et titulaire des compétences exigées par ce type d'intervention";

    private final AgenceRepository agenceRepository;
    private final EquipeRepository equipeRepository;
    private final TechnicienRepository technicienRepository;
    private final InterventionRepository interventionRepository;
    private final AffectationRepository affectationRepository;
    private final ExigenceCompetenceRepository exigenceRepository;
    private final ObtentionCompetenceRepository obtentionRepository;
    private final HistoriqueStatutRepository historiqueRepository;
    private final StatutRepository statutRepository;
    private final TypeInterventionRepository typeRepository;

    public RestitutionService(AgenceRepository agenceRepository, EquipeRepository equipeRepository,
                              TechnicienRepository technicienRepository,
                              InterventionRepository interventionRepository,
                              AffectationRepository affectationRepository,
                              ExigenceCompetenceRepository exigenceRepository,
                              ObtentionCompetenceRepository obtentionRepository,
                              HistoriqueStatutRepository historiqueRepository,
                              StatutRepository statutRepository,
                              TypeInterventionRepository typeRepository) {
        this.agenceRepository = agenceRepository;
        this.equipeRepository = equipeRepository;
        this.technicienRepository = technicienRepository;
        this.interventionRepository = interventionRepository;
        this.affectationRepository = affectationRepository;
        this.exigenceRepository = exigenceRepository;
        this.obtentionRepository = obtentionRepository;
        this.historiqueRepository = historiqueRepository;
        this.statutRepository = statutRepository;
        this.typeRepository = typeRepository;
    }

    /** F17 : techniciens libres et compétents, regroupés par agence avec leur charge du jour. */
    public DisponiblesResponse findDisponibles(LocalDateTime debut, LocalDateTime fin, String codeType) {
        if (debut == null || fin == null || !fin.isAfter(debut)) {
            throw new DonneeInvalideException("creneau-invalide",
                    "La fin du créneau doit être postérieure à son début");
        }
        TypeIntervention type = typeRepository.findByCode(codeType)
                .orElseThrow(() -> new DonneeInvalideException("type-intervention-inconnu",
                        "Le code de type d'intervention « " + codeType + " » est inconnu"));

        LocalDate jour = debut.toLocalDate();
        LocalDateTime debutJour = jour.atStartOfDay();
        LocalDateTime finJour = jour.plusDays(1).atStartOfDay();
        Map<Integer, List<TechnicienDisponibleResponse>> parAgence = new LinkedHashMap<>();
        for (Technicien technicien : technicienRepository.findAllByOrderByNomAsc()) {
            Equipe equipe = equipeRepository.findById(technicien.getIdEquipe()).orElse(null);
            if (equipe == null) {
                continue;
            }
            boolean libre = interventionRepository
                    .findChevauchements(technicien.getIdTechnicien(), debut, fin, -1).stream()
                    .noneMatch(i -> statutCourant(i) != StatutIntervention.ANNULEE);
            if (!libre || !competencesSatisfaites(technicien.getIdTechnicien(),
                    type.getIdType(), jour)) {
                continue;
            }
            BigDecimal charge = enHeures(heuresOccupees(interventionRepository.findChevauchements(
                    technicien.getIdTechnicien(), debutJour, finJour, -1).stream()
                    .filter(i -> statutCourant(i) != StatutIntervention.ANNULEE)
                    .toList(), debutJour, finJour));
            parAgence.computeIfAbsent(equipe.getIdAgence(), ignored -> new ArrayList<>())
                    .add(new TechnicienDisponibleResponse(technicien.getIdTechnicien(),
                            technicien.getNom(), technicien.getPrenom(), technicien.getMatricule(), charge));
        }

        List<AgenceDisponibleResponse> agences = parAgence.entrySet().stream()
                .map(e -> new AgenceDisponibleResponse(e.getKey(), nomAgence(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(AgenceDisponibleResponse::nomAgence))
                .toList();
        return new DisponiblesResponse(agences, agences.isEmpty() ? RAISON_AUCUN : null);
    }

    /** F18 : nombre d'interventions et heures occupées par technicien d'une agence, avec totaux. */
    public ChargeAgenceResponse findCharge(Integer idAgence, LocalDateTime du, LocalDateTime au) {
        Agence agence = requiredAgence(idAgence);
        if (du != null && au != null && au.isBefore(du)) {
            throw new DonneeInvalideException("periode-invalide",
                    "La date de fin ne peut pas être antérieure à la date de début");
        }
        LocalDateTime debut = du != null ? du : LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime fin = au != null ? au : LocalDateTime.of(2100, 1, 1, 0, 0);

        List<ChargeTechnicienResponse> lignes = new ArrayList<>();
        long totalInterventions = 0;
        Duration totalDuree = Duration.ZERO;
        for (Technicien technicien : techniciensDe(idAgence)) {
            List<Intervention> actives = interventionRepository
                    .findChevauchements(technicien.getIdTechnicien(), debut, fin, -1).stream()
                    .filter(i -> statutCourant(i) != StatutIntervention.ANNULEE)
                    .toList();
            Duration duree = heuresOccupees(actives, debut, fin);
            lignes.add(new ChargeTechnicienResponse(technicien.getIdTechnicien(),
                    technicien.getNom(), technicien.getPrenom(), actives.size(), enHeures(duree)));
            totalInterventions += actives.size();
            totalDuree = totalDuree.plus(duree);
        }
        return new ChargeAgenceResponse(idAgence, agence.getNom(), lignes,
                totalInterventions, enHeures(totalDuree));
    }

    /** F19 : taux mensuel TERMINEE / (TERMINEE + ECHOUEE), global et par type. */
    public TauxReussiteResponse findTauxReussite(Integer idAgence, String mois) {
        Agence agence = requiredAgence(idAgence);
        YearMonth periode;
        try {
            periode = YearMonth.parse(mois);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new DonneeInvalideException("mois-invalide",
                    "Le mois doit être au format yyyy-MM, par exemple 2026-11");
        }
        LocalDateTime debut = periode.atDay(1).atStartOfDay();
        LocalDateTime fin = periode.plusMonths(1).atDay(1).atStartOfDay();

        long terminees = 0;
        long echeouees = 0;
        Map<String, long[]> parType = new TreeMap<>();
        for (Technicien technicien : techniciensDe(idAgence)) {
            for (Intervention intervention : interventionRepository.findChevauchements(
                    technicien.getIdTechnicien(), debut, fin, -1)) {
                // F19 retient les interventions dont debut_prevu tombe dans le mois demandé.
                if (intervention.getDebutPrevu().isBefore(debut)
                        || !intervention.getDebutPrevu().isBefore(fin)) {
                    continue;
                }
                Affectation responsable = responsable(intervention.getIdIntervention());
                // Une intervention avec plusieurs affectations n'est comptée que pour son responsable (A-03).
                if (responsable == null
                        || !responsable.getIdTechnicien().equals(technicien.getIdTechnicien())) {
                    continue;
                }
                StatutIntervention statut = statutCourant(responsable);
                if (statut != StatutIntervention.TERMINEE && statut != StatutIntervention.ECHOUEE) {
                    continue;
                }
                boolean reussite = statut == StatutIntervention.TERMINEE;
                if (reussite) {
                    terminees++;
                } else {
                    echeouees++;
                }
                long[] compte = parType.computeIfAbsent(intervention.getCodeTypeIntervention(),
                        ignored -> new long[2]);
                compte[reussite ? 0 : 1]++;
            }
        }

        List<TauxTypeResponse> details = parType.entrySet().stream()
                .map(e -> new TauxTypeResponse(e.getKey(), e.getValue()[0], e.getValue()[1],
                        taux(e.getValue()[0], e.getValue()[1])))
                .toList();
        return new TauxReussiteResponse(idAgence, periode.toString(), terminees, echeouees,
                taux(terminees, echeouees), details);
    }

    private List<Technicien> techniciensDe(Integer idAgence) {
        List<Integer> idsEquipe = equipeRepository.findAllByIdAgence(idAgence).stream()
                .map(Equipe::getIdEquipe).toList();
        if (idsEquipe.isEmpty()) {
            return List.of();
        }
        return technicienRepository.findAllByIdEquipeInOrderByNomAsc(idsEquipe);
    }

    private Agence requiredAgence(Integer idAgence) {
        return agenceRepository.findById(idAgence)
                .orElseThrow(() -> new RessourceIntrouvableException("Agence", idAgence));
    }

    private String nomAgence(Integer idAgence) {
        return agenceRepository.findById(idAgence).map(Agence::getNom)
                .orElse("Agence " + idAgence);
    }

    private boolean competencesSatisfaites(Integer idTechnicien, Integer idType, LocalDate date) {
        List<ExigenceCompetence> exigences = exigenceRepository.findAllByIdIdType(idType);
        if (exigences.isEmpty()) {
            return true;
        }
        List<ObtentionCompetence> obtentions =
                obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(idTechnicien);
        for (ExigenceCompetence exigence : exigences) {
            Integer idCompetence = exigence.getId().getIdCompetence();
            boolean satisfaite = obtentions.stream()
                    .filter(o -> o.getIdCompetence().equals(idCompetence))
                    .anyMatch(o -> o.getDateExpiration() == null
                            || !o.getDateExpiration().isBefore(date));
            if (!satisfaite) {
                return false;
            }
        }
        return true;
    }

    private StatutIntervention statutCourant(Intervention intervention) {
        Affectation responsable = responsable(intervention.getIdIntervention());
        return responsable != null ? statutCourant(responsable) : StatutIntervention.PLANIFIEE;
    }

    private StatutIntervention statutCourant(Affectation responsable) {
        return historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(
                        responsable.getIdAffectation())
                .map(HistoriqueStatut::getIdStatut)
                .flatMap(statutRepository::findById)
                .map(s -> StatutIntervention.fromLibelle(s.getLibelle()))
                .orElse(StatutIntervention.PLANIFIEE);
    }

    private Affectation responsable(Integer idIntervention) {
        return affectationRepository.findByIdInterventionAndEstResponsableTrue(idIntervention)
                .orElse(null);
    }

    /** Somme des durées des interventions actives, ramenées à la période demandée. */
    private Duration heuresOccupees(List<Intervention> interventions, LocalDateTime debut,
                                    LocalDateTime fin) {
        Duration total = Duration.ZERO;
        for (Intervention intervention : interventions) {
            LocalDateTime debutUtile = intervention.getDebutPrevu().isBefore(debut)
                    ? debut : intervention.getDebutPrevu();
            LocalDateTime finUtile = intervention.getFinPrevue().isAfter(fin)
                    ? fin : intervention.getFinPrevue();
            if (finUtile.isAfter(debutUtile)) {
                total = total.plus(Duration.between(debutUtile, finUtile));
            }
        }
        return total;
    }

    private BigDecimal enHeures(Duration duree) {
        return BigDecimal.valueOf(duree.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal taux(long reussites, long echecs) {
        long total = reussites + echecs;
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(reussites * 100)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}

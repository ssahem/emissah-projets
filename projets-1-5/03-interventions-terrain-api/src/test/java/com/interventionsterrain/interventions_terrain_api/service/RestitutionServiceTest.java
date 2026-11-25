package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.response.ChargeAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.DisponiblesResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TauxReussiteResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Restitution du niveau 4 : F17, F18 et F19.
 *
 * COUCHE       : Service
 * RÔLE         : Restitution du niveau 4 : F17, F18 et F19.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class RestitutionServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final String CODE = "RACCORDEMENT_FIBRE";
    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 10, 9, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 10, 12, 0);

    @Mock private AgenceRepository agenceRepository;
    @Mock private EquipeRepository equipeRepository;
    @Mock private TechnicienRepository technicienRepository;
    @Mock private InterventionRepository interventionRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private ExigenceCompetenceRepository exigenceRepository;
    @Mock private ObtentionCompetenceRepository obtentionRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private StatutRepository statutRepository;
    @Mock private TypeInterventionRepository typeRepository;

    private RestitutionService service;
    private final Map<Integer, Affectation> responsables = new HashMap<>();
    private final Map<Integer, Integer> idStatutParAffectation = new HashMap<>();
    private final Map<Integer, String> libelleParIdStatut = new HashMap<>();

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new RestitutionService(agenceRepository, equipeRepository, technicienRepository,
                interventionRepository, affectationRepository, exigenceRepository, obtentionRepository,
                historiqueRepository, statutRepository, typeRepository);
        responsables.clear();
        idStatutParAffectation.clear();
        libelleParIdStatut.clear();

        lenient().when(affectationRepository.findByIdInterventionAndEstResponsableTrue(anyInt()))
                .thenAnswer(inv -> Optional.ofNullable(responsables.get(inv.getArgument(0))));
        lenient().when(historiqueRepository
                        .findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(anyInt()))
                .thenAnswer(inv -> {
                    Integer idStatut = idStatutParAffectation.get(inv.getArgument(0));
                    return idStatut == null ? Optional.empty()
                            : Optional.of(new HistoriqueStatut(DEBUT, idStatut, inv.getArgument(0)));
                });
        lenient().when(statutRepository.findById(anyInt()))
                .thenAnswer(inv -> {
                    String libelle = libelleParIdStatut.get(inv.getArgument(0));
                    return libelle == null ? Optional.empty() : Optional.of(statut(libelle));
                });
    }

    // ── F17 ──────────────────────────────────────────────────────────────────

    // ── f17DisponibleAvecChargeDuJour() ──────────────────────────────────────

    @Test
    @DisplayName("F17 : un créneau annulé n'empêche pas la disponibilité ; la charge exclut les annulations")
    void f17DisponibleAvecChargeDuJour() {
        Technicien technicien = technicien(3, "Alice", 10);
        preparerAgence(technicien, 1, "Agence Nord");
        TypeIntervention type = type(1);
        when(typeRepository.findByCode(CODE)).thenReturn(Optional.of(type));
        when(exigenceRepository.findAllByIdIdType(1)).thenReturn(List.of());

        Intervention annulee = intervention(60, DEBUT, DEBUT.plusHours(1));
        Intervention future = intervention(61, DEBUT.plusHours(5), FIN.plusHours(5));
        when(interventionRepository.findChevauchements(3, DEBUT, FIN, -1))
                .thenReturn(List.of(annulee));
        when(interventionRepository.findChevauchements(3,
                DEBUT.toLocalDate().atStartOfDay(), FIN.toLocalDate().plusDays(1).atStartOfDay(), -1))
                .thenReturn(List.of(annulee, future));
        statut(60, 3, "ANNULEE");
        statut(61, 3, "TERMINEE");

        DisponiblesResponse resultat = service.findDisponibles(DEBUT, FIN, CODE);

        assertThat(resultat.raisonFiltrage()).isNull();
        assertThat(resultat.agences()).hasSize(1);
        assertThat(resultat.agences().get(0).nomAgence()).isEqualTo("Agence Nord");
        assertThat(resultat.agences().get(0).techniciens()).hasSize(1);
        assertThat(resultat.agences().get(0).techniciens().get(0).chargeHeuresJour())
                .isEqualByComparingTo("3.00");
    }

    // ── f17IndisponiblePourChevauchement() ───────────────────────────────────

    @Test
    @DisplayName("F17 : un chevauchement non annulé rend le technicien indisponible")
    void f17IndisponiblePourChevauchement() {
        Technicien technicien = technicien(3, "Alice", 10);
        preparerAgence(technicien, 1, "Agence Nord");
        TypeIntervention type = type(1);
        when(typeRepository.findByCode(CODE)).thenReturn(Optional.of(type));

        Intervention bloquante = intervention(60, DEBUT.minusHours(1), FIN.minusHours(1));
        when(interventionRepository.findChevauchements(3, DEBUT, FIN, -1))
                .thenReturn(List.of(bloquante));
        statut(60, 3, "PLANIFIEE");

        DisponiblesResponse resultat = service.findDisponibles(DEBUT, FIN, CODE);

        assertThat(resultat.agences()).isEmpty();
        assertThat(resultat.raisonFiltrage()).contains("disponible", "compétences");
    }

    // ── f17CompetenceManquanteOuExpiree() ────────────────────────────────────

    @Test
    @DisplayName("F17 : une compétence exigée manquante ou expirée écarte le technicien")
    void f17CompetenceManquanteOuExpiree() {
        Technicien technicien = technicien(3, "Alice", 10);
        preparerAgence(technicien, 1, "Agence Nord");
        TypeIntervention type = type(1);
        when(typeRepository.findByCode(CODE)).thenReturn(Optional.of(type));
        when(exigenceRepository.findAllByIdIdType(1))
                .thenReturn(List.of(new ExigenceCompetence(1, 5)));
        when(obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(3))
                .thenReturn(List.of())
                .thenReturn(List.of(new ObtentionCompetence(LocalDate.of(2025, 1, 1),
                        LocalDate.of(2026, 12, 9), 3, 5)));

        assertThat(service.findDisponibles(DEBUT, FIN, CODE).agences()).isEmpty();
        assertThat(service.findDisponibles(DEBUT, FIN, CODE).agences()).isEmpty();
    }

    // ── f17ParametresInvalides() ─────────────────────────────────────────────

    @Test
    @DisplayName("F17 : un créneau inversé et un type inconnu sont refusés")
    void f17ParametresInvalides() {
        assertThatThrownBy(() -> service.findDisponibles(FIN, DEBUT, CODE))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("postérieure");
        assertThatThrownBy(() -> service.findDisponibles(DEBUT, FIN, "INCONNU"))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("inconnu");
    }

    // ── F18 ──────────────────────────────────────────────────────────────────

    // ── f18ChargeAgence() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F18 : la charge d'agence compte les interventions actives et exclut les annulées")
    void f18ChargeAgence() {
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Agence Nord")));
        when(equipeRepository.findAllByIdAgence(1)).thenReturn(List.of(equipe(10, 1), equipe(11, 1)));
        Technicien alice = technicien(3, "Alice", 10);
        Technicien bob = technicien(4, "Bob", 11);
        when(technicienRepository.findAllByIdEquipeInOrderByNomAsc(List.of(10, 11)))
                .thenReturn(List.of(alice, bob));

        LocalDateTime du = LocalDateTime.of(2026, 12, 1, 0, 0);
        LocalDateTime au = LocalDateTime.of(2026, 12, 2, 0, 0);
        Intervention active = intervention(60, LocalDateTime.of(2026, 12, 1, 9, 0),
                LocalDateTime.of(2026, 12, 1, 12, 0));
        Intervention annulee = intervention(61, LocalDateTime.of(2026, 12, 1, 14, 0),
                LocalDateTime.of(2026, 12, 1, 16, 0));
        when(interventionRepository.findChevauchements(3, du, au, -1))
                .thenReturn(List.of(active, annulee));
        when(interventionRepository.findChevauchements(4, du, au, -1)).thenReturn(List.of());
        statut(60, 3, "TERMINEE");
        statut(61, 3, "ANNULEE");

        ChargeAgenceResponse resultat = service.findCharge(1, du, au);

        assertThat(resultat.nomAgence()).isEqualTo("Agence Nord");
        assertThat(resultat.techniciens()).hasSize(2);
        assertThat(resultat.techniciens().get(0).nombreInterventions()).isEqualTo(1);
        assertThat(resultat.techniciens().get(0).heuresOccupees()).isEqualByComparingTo("3.00");
        assertThat(resultat.techniciens().get(1).nombreInterventions()).isZero();
        assertThat(resultat.nombreInterventionsTotal()).isEqualTo(1);
        assertThat(resultat.heuresOccupeesTotales()).isEqualByComparingTo("3.00");
    }

    // ── f18AgenceInconnueEtPeriodeInversee() ─────────────────────────────────

    @Test
    @DisplayName("F18 : une agence inconnue renvoie 404 et une période inversée est refusée")
    void f18AgenceInconnueEtPeriodeInversee() {
        assertThatThrownBy(() -> service.findCharge(99, null, null))
                .isInstanceOf(RessourceIntrouvableException.class);

        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Agence Nord")));
        assertThatThrownBy(() -> service.findCharge(1, FIN, DEBUT))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("antérieure");
    }

    // ── F19 ──────────────────────────────────────────────────────────────────

    // ── f19TauxMensuel() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : taux global et par type, hors interventions non clôturées et hors mois")
    void f19TauxMensuel() {
        preparerAgenceUnTechnicien();
        LocalDateTime debut = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 12, 1, 0, 0);
        Intervention terminee = intervention(60, "TYPE_A", LocalDateTime.of(2026, 11, 10, 9, 0));
        Intervention echeouee = intervention(61, "TYPE_A", LocalDateTime.of(2026, 11, 11, 9, 0));
        Intervention annulee = intervention(62, "TYPE_A", LocalDateTime.of(2026, 11, 12, 9, 0));
        Intervention enCours = intervention(63, "TYPE_B", LocalDateTime.of(2026, 11, 13, 9, 0));
        Intervention moisPrecedent = intervention(64, "TYPE_A", LocalDateTime.of(2026, 10, 31, 9, 0));
        when(interventionRepository.findChevauchements(3, debut, fin, -1))
                .thenReturn(List.of(terminee, echeouee, annulee, enCours, moisPrecedent));
        statut(60, 3, "TERMINEE");
        statut(61, 3, "ECHOUEE");
        statut(62, 3, "ANNULEE");
        statut(63, 3, "EN_COURS");
        statut(64, 3, "TERMINEE");

        TauxReussiteResponse resultat = service.findTauxReussite(1, "2026-11");

        assertThat(resultat.terminees()).isEqualTo(1);
        assertThat(resultat.echeouees()).isEqualTo(1);
        assertThat(resultat.tauxGlobal()).isEqualByComparingTo("50.00");
        assertThat(resultat.parType()).hasSize(1);
        assertThat(resultat.parType().get(0).codeType()).isEqualTo("TYPE_A");
        assertThat(resultat.parType().get(0).taux()).isEqualByComparingTo("50.00");
    }

    // ── f19AucuneCloturee() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : une agence sans intervention clôturée renvoie un taux à zéro")
    void f19AucuneCloturee() {
        preparerAgenceUnTechnicien();
        when(interventionRepository.findChevauchements(3,
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0), -1))
                .thenReturn(List.of());

        TauxReussiteResponse resultat = service.findTauxReussite(1, "2026-11");

        assertThat(resultat.tauxGlobal()).isEqualByComparingTo("0.00");
        assertThat(resultat.parType()).isEmpty();
    }

    // ── f19ParametresInvalides() ─────────────────────────────────────────────

    @Test
    @DisplayName("F19 : un mois invalide est refusé, une agence inconnue renvoie 404")
    void f19ParametresInvalides() {
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Agence Nord")));
        assertThatThrownBy(() -> service.findTauxReussite(1, "11/2026"))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("yyyy-MM");
        assertThatThrownBy(() -> service.findTauxReussite(99, "2026-11"))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── preparerAgence() ─────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour preparerAgence().
     */

    private void preparerAgence(Technicien technicien, Integer idAgence, String nom) {
        lenient().when(agenceRepository.findById(idAgence))
                .thenReturn(Optional.of(agence(idAgence, nom)));
        when(technicienRepository.findAllByOrderByNomAsc()).thenReturn(List.of(technicien));
        when(equipeRepository.findById(technicien.getIdEquipe()))
                .thenReturn(Optional.of(equipe(technicien.getIdEquipe(), idAgence)));
    }

    // ── preparerAgenceUnTechnicien() ─────────────────────────────────────────

    /**
     * Prépare la donnée de test pour preparerAgenceUnTechnicien().
     */

    private void preparerAgenceUnTechnicien() {
        Technicien technicien = technicien(3, "Alice", 10);
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Agence Nord")));
        when(equipeRepository.findAllByIdAgence(1)).thenReturn(List.of(equipe(10, 1)));
        when(technicienRepository.findAllByIdEquipeInOrderByNomAsc(List.of(10)))
                .thenReturn(List.of(technicien));
    }

    // ── statut() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour statut().
     */

    private void statut(Integer idIntervention, Integer idTechnicien, String libelle) {
        Affectation affectation = affectation(idIntervention + 100, idIntervention, idTechnicien);
        responsables.put(idIntervention, affectation);
        int idStatut = 200 + idIntervention;
        idStatutParAffectation.put(affectation.getIdAffectation(), idStatut);
        libelleParIdStatut.put(idStatut, libelle);
    }

    // ── agence() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour agence().
     */

    private Agence agence(Integer id, String nom) {
        Agence agence = new Agence(nom, nom, "Paris", null);
        ReflectionTestUtils.setField(agence, "idAgence", id);
        return agence;
    }

    // ── equipe() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour equipe().
     */

    private Equipe equipe(Integer id, Integer idAgence) {
        Equipe equipe = new Equipe("Équipe " + id, idAgence);
        ReflectionTestUtils.setField(equipe, "idEquipe", id);
        return equipe;
    }

    // ── technicien() ─────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour technicien().
     */

    private Technicien technicien(Integer id, String nom, Integer idEquipe) {
        Technicien technicien = new Technicien(nom, "Prenom", nom.toLowerCase() + "@example.test",
                "0102030405", idEquipe, "MAT" + id);
        ReflectionTestUtils.setField(technicien, "idTechnicien", id);
        return technicien;
    }

    // ── type() ───────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour type().
     */

    private TypeIntervention type(Integer id) {
        TypeIntervention type = mock(TypeIntervention.class);
        lenient().when(type.getIdType()).thenReturn(id);
        lenient().when(type.getCode()).thenReturn(CODE);
        return type;
    }

    // ── intervention() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour intervention().
     */

    private Intervention intervention(Integer id, LocalDateTime debut) {
        return intervention(id, CODE, debut);
    }

    // ── intervention() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour intervention().
     */

    private Intervention intervention(Integer id, String code, LocalDateTime debut) {
        return intervention(id, code, debut, debut.plusHours(3));
    }

    // ── intervention() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour intervention().
     */

    private Intervention intervention(Integer id, LocalDateTime debut, LocalDateTime fin) {
        return intervention(id, CODE, debut, fin);
    }

    // ── intervention() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour intervention().
     */

    private Intervention intervention(Integer id, String code, LocalDateTime debut, LocalDateTime fin) {
        Intervention intervention = new Intervention(LocalDateTime.of(2026, 11, 1, 8, 0),
                debut, fin, 7, 50, code);
        ReflectionTestUtils.setField(intervention, "idIntervention", id);
        return intervention;
    }

    // ── affectation() ────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour affectation().
     */

    private Affectation affectation(Integer id, Integer idIntervention, Integer idTechnicien) {
        Affectation affectation = new Affectation(idIntervention, idTechnicien, true);
        ReflectionTestUtils.setField(affectation, "idAffectation", id);
        return affectation;
    }

    // ── statut() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour statut().
     */

    private Statut statut(String libelle) {
        Statut statut = mock(Statut.class);
        lenient().when(statut.getLibelle()).thenReturn(libelle);
        return statut;
    }
}

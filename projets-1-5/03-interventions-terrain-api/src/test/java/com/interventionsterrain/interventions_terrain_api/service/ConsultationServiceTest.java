package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.HistoriqueClientResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.PlanningInterventionResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienEffectifResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.exception.RessourceIntrouvableException;
import com.interventionsterrain.interventions_terrain_api.mapper.AffectationMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.AgenceMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.ClientMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.EquipeLookup;
import com.interventionsterrain.interventions_terrain_api.mapper.InterventionMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.TechnicienMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TypeInterventionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Règles des consultations F6 à F10.
 *
 * COUCHE       : Service
 * RÔLE         : Règles des consultations F6 à F10.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 11, 24);

    @Mock private AgenceRepository agenceRepository;
    @Mock private EquipeRepository equipeRepository;
    @Mock private TechnicienRepository technicienRepository;
    @Mock private ObtentionCompetenceRepository obtentionRepository;
    @Mock private CompetenceRepository competenceRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private InterventionRepository interventionRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private AdresseRepository adresseRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private StatutRepository statutRepository;
    @Mock private TypeInterventionRepository typeRepository;
    @Mock private com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository exigenceRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);

    private AgenceService agenceService;
    private TechnicienService technicienService;
    private ClientService clientService;
    private InterventionService interventionService;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        AgenceMapper agenceMapper = new AgenceMapper();
        TechnicienMapper technicienMapper = new TechnicienMapper(new EquipeLookup(equipeRepository, agenceRepository));
        agenceService = new AgenceService(agenceRepository, equipeRepository, technicienRepository,
                obtentionRepository, agenceMapper, clock);
        technicienService = new TechnicienService(technicienRepository, equipeRepository, agenceRepository,
                affectationRepository, obtentionRepository, competenceRepository, interventionRepository,
                adresseRepository, clientRepository, historiqueRepository, statutRepository,
                technicienMapper, clock);
        clientService = new ClientService(clientRepository, interventionRepository, affectationRepository,
                technicienRepository, historiqueRepository, statutRepository, new ClientMapper());
        interventionService = new InterventionService(interventionRepository, adresseRepository,
                clientRepository, technicienRepository, typeRepository, statutRepository,
                affectationRepository, historiqueRepository, exigenceRepository, obtentionRepository,
                competenceRepository, new InterventionMapper(), new AffectationMapper(), clock);
    }

    // ── F6 ───────────────────────────────────────────────────────────────────

    // ── f6EffectifAgence() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F6 : l'effectif d'une agence est trié et compte les compétences valides")
    void f6EffectifAgence() {
        Agence agence = agence(1, "Paris Centre");
        Equipe equipe1 = equipe(10, 1);
        Equipe equipe2 = equipe(11, 1);
        Technicien alice = technicien(3, "Alice", 10);
        Technicien bob = technicien(4, "Bob", 11);
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence));
        when(equipeRepository.findAllByIdAgence(1)).thenReturn(List.of(equipe1, equipe2));
        when(technicienRepository.findAllByIdEquipeInOrderByNomAsc(List.of(10, 11)))
                .thenReturn(List.of(alice, bob));
        when(obtentionRepository.countCompetencesValides(3, AUJOURD_HUI)).thenReturn(2L);
        when(obtentionRepository.countCompetencesValides(4, AUJOURD_HUI)).thenReturn(0L);

        List<TechnicienEffectifResponse> resultat = agenceService.findTechniciens(1);

        assertThat(resultat).extracting(TechnicienEffectifResponse::nom)
                .containsExactly("Alice", "Bob");
        assertThat(resultat).extracting(TechnicienEffectifResponse::nbCompetencesValides)
                .containsExactly(2L, 0L);
    }

    // ── f6AgenceInconnueEtSansEquipe() ───────────────────────────────────────

    @Test
    @DisplayName("F6 : une agence inconnue renvoie 404 et une agence sans équipe une liste vide")
    void f6AgenceInconnueEtSansEquipe() {
        when(agenceRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> agenceService.findTechniciens(99))
                .isInstanceOf(RessourceIntrouvableException.class);

        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Paris Centre")));
        when(equipeRepository.findAllByIdAgence(1)).thenReturn(List.of());
        assertThat(agenceService.findTechniciens(1)).isEmpty();
    }

    // ── F7 ───────────────────────────────────────────────────────────────────

    // ── f7CompetencesExpireesSignalees() ─────────────────────────────────────

    @Test
    @DisplayName("F7 : les compétences expirées sont signalées et jamais masquées")
    void f7CompetencesExpireesSignalees() {
        Competence fibre = competence(5, "Fibre");
        Competence cuivre = competence(6, "Cuivre");
        ObtentionCompetence fibreExpiree = obtention(3, 5,
                LocalDate.of(2024, 1, 10), LocalDate.of(2025, 1, 10));
        ObtentionCompetence cuivreValide = obtention(3, 6,
                LocalDate.of(2025, 6, 1), LocalDate.of(2027, 6, 1));
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(3))
                .thenReturn(List.of(fibreExpiree, cuivreValide));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(fibre));
        when(competenceRepository.findById(6)).thenReturn(Optional.of(cuivre));

        List<CompetenceTechnicienResponse> resultat = technicienService.findCompetences(3);

        assertThat(resultat).hasSize(2);
        assertThat(resultat).extracting(CompetenceTechnicienResponse::expiree)
                .containsExactly(true, false);
        assertThat(resultat.get(0).libelle()).isEqualTo("Fibre");
        assertThat(resultat.get(1).libelle()).isEqualTo("Cuivre");
    }

    // ── F8 ───────────────────────────────────────────────────────────────────

    // ── f8HistoriqueClient() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F8 : l'historique client est trié du plus récent au plus ancien avec statut et responsable")
    void f8HistoriqueClient() {
        Client client = client(7, "Dupont");
        Intervention recente = intervention(20, 7, LocalDateTime.of(2026, 12, 2, 9, 0));
        Intervention ancienne = intervention(21, 7, LocalDateTime.of(2026, 11, 2, 9, 0));
        Affectation responsable = affectation(100, 3, true);
        Statut terminee = statut("TERMINEE");
        when(clientRepository.findById(7)).thenReturn(Optional.of(client));
        when(interventionRepository.findAllByIdClientOrderByDateDemandeDesc(7))
                .thenReturn(List.of(recente, ancienne));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(21))
                .thenReturn(Optional.empty());
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(100))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 12, 3, 9, 0), 3, 100)));
        when(statutRepository.findById(3)).thenReturn(Optional.of(terminee));

        List<HistoriqueClientResponse> resultat = clientService.findHistorique(7);

        assertThat(resultat).extracting(HistoriqueClientResponse::idIntervention).containsExactly(20, 21);
        assertThat(resultat.get(0).statut()).isEqualTo("TERMINEE");
        assertThat(resultat.get(0).nomTechnicienResponsable()).isEqualTo("Alice Prenom");
        assertThat(resultat.get(1).statut()).isNull();
        assertThat(resultat.get(1).idTechnicienResponsable()).isNull();
    }

    // ── F9 ───────────────────────────────────────────────────────────────────

    // ── f9FiltresCombinables() ───────────────────────────────────────────────

    @Test
    @DisplayName("F9 : les filtres sont combinables et délégués à la requête native")
    void f9FiltresCombinables() {
        when(interventionRepository.findAllFiltres("PLANIFIEE",
                LocalDateTime.of(2026, 12, 1, 0, 0), LocalDateTime.of(2026, 12, 8, 0, 0)))
                .thenReturn(List.of(intervention(20, 7, LocalDateTime.of(2026, 12, 2, 9, 0))));

        List<InterventionResponse> resultat = interventionService.findAll("PLANIFIEE",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 7));

        assertThat(resultat).hasSize(1);
        verify(interventionRepository).findAllFiltres("PLANIFIEE",
                LocalDateTime.of(2026, 12, 1, 0, 0), LocalDateTime.of(2026, 12, 8, 0, 0));
    }

    // ── f9FiltresInvalides() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : un statut inconnu ou une période inversée sont refusés")
    void f9FiltresInvalides() {
        assertThatThrownBy(() -> interventionService.findAll("INCONNU", null, null))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("inconnu");
        assertThatThrownBy(() -> interventionService.findAll(null,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 1)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("postérieure");
    }

    // ── F10 ──────────────────────────────────────────────────────────────────

    // ── f10PlanningDefaut() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F10 : le planning défaut couvre la semaine courante et signale les annulations")
    void f10PlanningDefaut() {
        Intervention annulee = intervention(20, 7, LocalDateTime.of(2026, 11, 20, 9, 0));
        Intervention planifiee = intervention(21, 7, LocalDateTime.of(2026, 11, 21, 9, 0));
        Affectation responsableAnnulee = affectationSur(100, 20, 3, true);
        Affectation responsablePlanifiee = affectationSur(101, 21, 3, true);
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(affectationRepository.findAllByIdTechnicien(3))
                .thenReturn(List.of(responsableAnnulee, responsablePlanifiee));
        when(interventionRepository.findAllByIdInterventionInOrderByDebutPrevuAsc(List.of(20, 21)))
                .thenReturn(List.of(annulee, planifiee));
        when(clientRepository.findById(7)).thenReturn(Optional.of(client(7, "Dupont")));
        when(adresseRepository.findById(any())).thenReturn(Optional.of(new Adresse("12 rue de la Paix", "Paris")));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsableAnnulee));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(21))
                .thenReturn(Optional.of(responsablePlanifiee));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(100))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 6, 100)));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(101))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 1, 101)));
        Statut ligneAnnulee = statut("ANNULEE");
        Statut lignePlanifiee = statut("PLANIFIEE");
        when(statutRepository.findById(6)).thenReturn(Optional.of(ligneAnnulee));
        when(statutRepository.findById(1)).thenReturn(Optional.of(lignePlanifiee));

        List<PlanningInterventionResponse> resultat = technicienService.findPlanning(3, null, null);

        assertThat(resultat).extracting(PlanningInterventionResponse::idIntervention).containsExactly(20, 21);
        assertThat(resultat).extracting(PlanningInterventionResponse::annulee).containsExactly(true, false);
        assertThat(resultat.get(0).voie()).isEqualTo("12 rue de la Paix");
    }

    // ── f10PeriodeTropLongue() ───────────────────────────────────────────────

    @Test
    @DisplayName("F10 : une période de plus de trois mois est refusée")
    void f10PeriodeTropLongue() {
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));

        assertThatThrownBy(() -> technicienService.findPlanning(3,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("trois mois");
    }

    // ── Fabriques ────────────────────────────────────────────────────────────

    // ── agence() ─────────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Agence agence(Integer id, String nom) {
        Agence agence = new Agence(nom, nom, "Paris", null);
        ReflectionTestUtils.setField(agence, "idAgence", id);
        return agence;
    }

    // ── equipe() ─────────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Equipe equipe(Integer id, Integer idAgence) {
        Equipe equipe = new Equipe("Équipe " + id, idAgence);
        ReflectionTestUtils.setField(equipe, "idEquipe", id);
        return equipe;
    }

    // ── technicien() ─────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Technicien technicien(Integer id, String nom, Integer idEquipe) {
        Technicien technicien = new Technicien(nom, "Prenom", nom.toLowerCase() + "@example.test",
                "0102030405", idEquipe, "MAT" + id);
        ReflectionTestUtils.setField(technicien, "idTechnicien", id);
        return technicien;
    }

    // ── competence() ─────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Competence competence(Integer id, String libelle) {
        Competence competence = new Competence(libelle, null);
        ReflectionTestUtils.setField(competence, "idCompetence", id);
        return competence;
    }

    // ── obtention() ──────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private ObtentionCompetence obtention(Integer idTechnicien, Integer idCompetence,
                                          LocalDate obtention, LocalDate expiration) {
        ObtentionCompetence resultat = mock(ObtentionCompetence.class);
        lenient().when(resultat.getIdTechnicien()).thenReturn(idTechnicien);
        lenient().when(resultat.getIdCompetence()).thenReturn(idCompetence);
        lenient().when(resultat.getDateObtention()).thenReturn(obtention);
        lenient().when(resultat.getDateExpiration()).thenReturn(expiration);
        return resultat;
    }

    // ── client() ─────────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Client client(Integer id, String nom) {
        Client client = new Client(nom, "Marie", "client@example.test", "0102030405", "5 avenue du parc", "Paris");
        ReflectionTestUtils.setField(client, "idClient", id);
        return client;
    }

    // ── intervention() ───────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Intervention intervention(Integer id, Integer idClient, LocalDateTime dateDemande) {
        Intervention intervention = new Intervention(dateDemande, dateDemande.plusDays(5),
                dateDemande.plusDays(5).plusHours(3), idClient, 50, "RACCORDEMENT_FIBRE");
        ReflectionTestUtils.setField(intervention, "idIntervention", id);
        return intervention;
    }

    // ── affectation() ────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Affectation affectation(Integer id, Integer idTechnicien, boolean responsable) {
        return affectationSur(id, 20, idTechnicien, responsable);
    }

    // ── affectationSur() ─────────────────────────────────────────────────────

    /**
     * Prépare le contexte nécessaire à l’opération affectationSur().
     */

    private Affectation affectationSur(Integer id, Integer idIntervention, Integer idTechnicien,
                                       boolean responsable) {
        Affectation affectation = new Affectation(idIntervention, idTechnicien, responsable);
        ReflectionTestUtils.setField(affectation, "idAffectation", id);
        return affectation;
    }

    // ── statut() ─────────────────────────────────────────────────────────────

    /**
     * Prépare un consultation de test avec des valeurs nominales.
     */

    private Statut statut(String libelle) {
        Statut statut = mock(Statut.class);
        lenient().when(statut.getLibelle()).thenReturn(libelle);
        return statut;
    }
}

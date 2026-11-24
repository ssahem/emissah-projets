package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateObtentionCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateRepriseRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateStatutRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AlerteAgenceResponse;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceTechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetenceId;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.ApiException;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.mapper.AffectationMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.EquipeLookup;
import com.interventionsterrain.interventions_terrain_api.mapper.InterventionMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.TechnicienMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Règles métier du niveau 3 : F11 à F16. */
@ExtendWith(MockitoExtension.class)
class ReglesMetierServiceTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 11, 24);
    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 10, 9, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 10, 12, 0);

    @Mock private TechnicienRepository technicienRepository;
    @Mock private EquipeRepository equipeRepository;
    @Mock private AgenceRepository agenceRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private ObtentionCompetenceRepository obtentionRepository;
    @Mock private CompetenceRepository competenceRepository;
    @Mock private InterventionRepository interventionRepository;
    @Mock private AdresseRepository adresseRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private StatutRepository statutRepository;
    @Mock private TypeInterventionRepository typeRepository;
    @Mock private ExigenceCompetenceRepository exigenceRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);

    private TechnicienService technicienService;
    private InterventionService interventionService;
    private AlerteCompetenceService alerteService;

    @BeforeEach
    void setUp() {
        technicienService = new TechnicienService(technicienRepository, equipeRepository, agenceRepository,
                affectationRepository, obtentionRepository, competenceRepository, interventionRepository,
                adresseRepository, clientRepository, historiqueRepository, statutRepository,
                new TechnicienMapper(new EquipeLookup(equipeRepository, agenceRepository)), clock);
        interventionService = new InterventionService(interventionRepository, adresseRepository,
                clientRepository, technicienRepository, typeRepository, statutRepository,
                affectationRepository, historiqueRepository, exigenceRepository, obtentionRepository,
                competenceRepository, new InterventionMapper(), new AffectationMapper(), clock);
        alerteService = new AlerteCompetenceService(obtentionRepository, technicienRepository,
                equipeRepository, agenceRepository, competenceRepository, clock);
    }

    // ── F11 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F11 : une expiration antérieure à l'obtention est refusée")
    void f11DatesIncoherentes() {
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(competence(5, "Fibre")));

        assertThatThrownBy(() -> technicienService.associerCompetence(3,
                new CreateObtentionCompetenceRequest(5,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2025, 1, 10))))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("postérieure");
    }

    @Test
    @DisplayName("F11 : un doublon de compétence active est refusé avec un 409")
    void f11DoublonActif() {
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(competence(5, "Fibre")));
        when(obtentionRepository.existsCompetenceActive(3, 5, AUJOURD_HUI)).thenReturn(true);

        assertThatThrownBy(() -> technicienService.associerCompetence(3,
                new CreateObtentionCompetenceRequest(5, LocalDate.of(2026, 1, 10), null)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà");
    }

    @Test
    @DisplayName("F11 : une compétence expirée peut être redemandée et est persistée")
    void f11AssociationReussie() {
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(competence(5, "Fibre")));
        when(obtentionRepository.save(any(ObtentionCompetence.class))).thenAnswer(invocation -> {
            ObtentionCompetence o = invocation.getArgument(0);
            ReflectionTestUtils.setField(o, "idObtention", 50);
            return o;
        });

        CompetenceTechnicienResponse resultat = technicienService.associerCompetence(3,
                new CreateObtentionCompetenceRequest(5,
                        LocalDate.of(2025, 1, 10), LocalDate.of(2026, 1, 10)));

        assertThat(resultat.libelle()).isEqualTo("Fibre");
        assertThat(resultat.expiree()).isTrue();
        ArgumentCaptor<ObtentionCompetence> captor = ArgumentCaptor.forClass(ObtentionCompetence.class);
        verify(obtentionRepository).save(captor.capture());
        assertThat(captor.getValue().getIdTechnicien()).isEqualTo(3);
        assertThat(captor.getValue().getIdCompetence()).isEqualTo(5);
    }

    // ── F12 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F12 : un chevauchement renvoie 409 avec l'intervention bloquante")
    void f12ChevauchementRefuse() {
        Intervention bloquante = intervention(60, DEBUT.minusHours(1), FIN.minusHours(1));
        TypeIntervention type = type(1, "RACCORDEMENT_FIBRE");
        when(clientRepository.existsById(7)).thenReturn(true);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(technicienRepository.existsById(3)).thenReturn(true);
        when(interventionRepository.findChevauchements(3, DEBUT, FIN, -1))
                .thenReturn(List.of(bloquante));

        assertThatThrownBy(() -> interventionService.create(new CreateInterventionRequest(
                7, "RACCORDEMENT_FIBRE", 3, LocalDateTime.of(2026, 11, 24, 9, 0),
                DEBUT, FIN, "12 rue de la Paix", "Paris")))
                .isInstanceOf(ConflitMetierException.class)
                .satisfies(e -> assertThat(((ApiException) e).getProperties())
                        .containsEntry("idInterventionBloquant", 60)
                        .containsEntry("debut", bloquante.getDebutPrevu())
                        .containsEntry("fin", bloquante.getFinPrevue()));
    }

    @Test
    @DisplayName("F12 : une intervention ANNULEE ne bloque pas le créneau")
    void f12AnnuleeIgnoree() {
        Intervention annulee = intervention(60, DEBUT, FIN);
        Affectation responsable = affectation(100, 60, 3);
        prepareCreate();
        when(interventionRepository.findChevauchements(3, DEBUT, FIN, -1)).thenReturn(List.of(annulee));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(60))
                .thenReturn(Optional.of(responsable));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.empty());
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(100))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 6, 100)));
        Statut ligneAnnulee = statut("ANNULEE");
        when(statutRepository.findById(6)).thenReturn(Optional.of(ligneAnnulee));

        interventionService.create(new CreateInterventionRequest(7, "RACCORDEMENT_FIBRE", 3,
                LocalDateTime.of(2026, 11, 24, 9, 0), DEBUT, FIN, "12 rue de la Paix", "Paris"));

        verify(interventionRepository).save(any(Intervention.class));
    }

    // ── F13 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F13 : les compétences exigées manquantes ou expirées sont listées dans le 400")
    void f13CompetencesManquantes() {
        TypeIntervention type = type(1, "RACCORDEMENT_FIBRE");
        when(clientRepository.existsById(7)).thenReturn(true);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(technicienRepository.existsById(3)).thenReturn(true);
        when(exigenceRepository.findAllByIdIdType(1))
                .thenReturn(List.of(new ExigenceCompetence(1, 5), new ExigenceCompetence(1, 6)));
        ObtentionCompetence expiree = new ObtentionCompetence(
                LocalDate.of(2024, 1, 10), LocalDate.of(2025, 1, 10), 3, 5);
        when(obtentionRepository.findAllByIdTechnicienOrderByDateObtentionDesc(3))
                .thenReturn(List.of(expiree));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(competence(5, "Fibre")));
        when(competenceRepository.findById(6)).thenReturn(Optional.of(competence(6, "Cuivre")));

        assertThatThrownBy(() -> interventionService.create(new CreateInterventionRequest(
                7, "RACCORDEMENT_FIBRE", 3, LocalDateTime.of(2026, 11, 24, 9, 0),
                DEBUT, FIN, "12 rue de la Paix", "Paris")))
                .isInstanceOf(DonneeInvalideException.class)
                .satisfies(e -> assertThat(((ApiException) e).getProperties())
                        .containsEntry("competences", List.of("Fibre", "Cuivre")));
    }

    @Test
    @DisplayName("F13 : un type sans exigence accepte tout technicien")
    void f13SansExigence() {
        prepareCreate();

        interventionService.create(new CreateInterventionRequest(7, "RACCORDEMENT_FIBRE", 3,
                LocalDateTime.of(2026, 11, 24, 9, 0), DEBUT, FIN, "12 rue de la Paix", "Paris"));

        verify(interventionRepository).save(any(Intervention.class));
    }

    // ── F14 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : PLANIFIEE → EN_COURS est horodaté sur l'affectation responsable")
    void f14TransitionAutorisée() {
        Intervention intervention = intervention(20, DEBUT, FIN);
        Affectation responsable = affectation(10, 20, 3);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        Statut enCours = statut("EN_COURS");
        when(statutRepository.findByLibelle("EN_COURS")).thenReturn(Optional.of(enCours));

        interventionService.changerStatut(20, new UpdateStatutRequest("EN_COURS"));

        ArgumentCaptor<HistoriqueStatut> captor = ArgumentCaptor.forClass(HistoriqueStatut.class);
        verify(historiqueRepository).save(captor.capture());
        assertThat(captor.getValue().getIdAffectation()).isEqualTo(10);
        assertThat(captor.getValue().getDateHeure()).isEqualTo(LocalDateTime.of(2026, 11, 24, 10, 0));
    }

    @Test
    @DisplayName("F14 : une transition interdite indique les statuts atteignables")
    void f14TransitionInterdite() {
        Intervention intervention = intervention(20, DEBUT, FIN);
        Affectation responsable = affectation(10, 20, 3);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(10))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 3, 20)));
        Statut enCours = statut("EN_COURS");
        when(statutRepository.findById(3)).thenReturn(Optional.of(enCours));

        assertThatThrownBy(() -> interventionService.changerStatut(20, new UpdateStatutRequest("ANNULEE")))
                .isInstanceOf(ConflitMetierException.class)
                .satisfies(e -> assertThat(((ApiException) e).getProperties())
                        .containsEntry("statutCourant", "EN_COURS")
                        .containsEntry("atteignables", List.of("ECHOUEE", "TERMINEE")));
    }

    @Test
    @DisplayName("F14 : les statuts hérités et inconnus ne sont pas acceptés en écriture")
    void f14StatutsHeritesEtInconnus() {
        Intervention intervention = intervention(20, DEBUT, FIN);
        Affectation responsable = affectation(10, 20, 3);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention));
        lenient().when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));

        assertThatThrownBy(() -> interventionService.changerStatut(20, new UpdateStatutRequest("EN_ROUTE")))
                .isInstanceOf(ConflitMetierException.class);
        assertThatThrownBy(() -> interventionService.changerStatut(20, new UpdateStatutRequest("INCONNU")))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── F15 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : une reprise reprend client, type et responsable et référence l'origine")
    void f15RepriseReussie() {
        Intervention origine = intervention(20, DEBUT, FIN);
        Affectation responsable = affectation(10, 20, 3);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(origine));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(10))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 5, 10)));
        Statut echec = statut("ECHOUEE");
        Statut planifiee = statut("PLANIFIEE");
        TypeIntervention type = type(1, "RACCORDEMENT_FIBRE");
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(statutRepository.findById(5)).thenReturn(Optional.of(echec));
        when(statutRepository.findByLibelle("PLANIFIEE")).thenReturn(Optional.of(planifiee));
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> {
            Intervention i = invocation.getArgument(0);
            ReflectionTestUtils.setField(i, "idIntervention", 21);
            return i;
        });
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            Affectation a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "idAffectation", 11);
            return a;
        });

        interventionService.creerReprise(20, new CreateRepriseRequest(
                LocalDateTime.of(2026, 12, 17, 9, 0), LocalDateTime.of(2026, 12, 17, 12, 0)));

        ArgumentCaptor<Intervention> captor = ArgumentCaptor.forClass(Intervention.class);
        verify(interventionRepository).save(captor.capture());
        assertThat(captor.getValue().getIdInterventionOrigine()).isEqualTo(20);
        assertThat(captor.getValue().getIdClient()).isEqualTo(7);
        assertThat(captor.getValue().getCodeTypeIntervention()).isEqualTo("RACCORDEMENT_FIBRE");
        verify(affectationRepository).save(any(Affectation.class));
    }

    @Test
    @DisplayName("F15 : une intervention non échouée ou déjà reprise ne peut pas être reprise")
    void f15RefusReprise() {
        Intervention origine = intervention(20, DEBUT, FIN);
        Affectation responsable = affectation(10, 20, 3);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(origine));
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(10))
                .thenReturn(Optional.of(new HistoriqueStatut(LocalDateTime.of(2026, 11, 24, 9, 0), 1, 10)));
        Statut planifiee = statut("PLANIFIEE");
        when(statutRepository.findById(1)).thenReturn(Optional.of(planifiee));

        assertThatThrownBy(() -> interventionService.creerReprise(20, new CreateRepriseRequest(DEBUT, FIN)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("ECHOUEE");
    }

    // ── F16 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : les alertes sont regroupées par agence et marquent date_alerte")
    void f16AlertesParAgence() {
        ObtentionCompetence fibre = new ObtentionCompetence(
                LocalDate.of(2025, 1, 10), LocalDate.of(2026, 12, 10), 3, 5);
        ObtentionCompetence cuivre = new ObtentionCompetence(
                LocalDate.of(2025, 2, 10), LocalDate.of(2026, 12, 20), 4, 6);
        when(obtentionRepository.findAllByDateExpirationBetweenAndDateAlerteIsNull(
                AUJOURD_HUI, AUJOURD_HUI.plusDays(30))).thenReturn(List.of(fibre, cuivre));
        when(technicienRepository.findById(3)).thenReturn(Optional.of(technicien(3, "Alice", 10)));
        when(technicienRepository.findById(4)).thenReturn(Optional.of(technicien(4, "Bob", 11)));
        when(equipeRepository.findById(10)).thenReturn(Optional.of(equipe(10, 1)));
        when(equipeRepository.findById(11)).thenReturn(Optional.of(equipe(11, 1)));
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Paris Centre")));
        when(competenceRepository.findById(5)).thenReturn(Optional.of(competence(5, "Fibre")));
        when(competenceRepository.findById(6)).thenReturn(Optional.of(competence(6, "Cuivre")));

        List<AlerteAgenceResponse> alertes = alerteService.genererAlertes();

        assertThat(alertes).hasSize(1);
        assertThat(alertes.get(0).nomAgence()).isEqualTo("Paris Centre");
        assertThat(alertes.get(0).lignes()).extracting(l -> l.libelleCompetence())
                .containsExactly("Fibre", "Cuivre");
        assertThat(fibre.getDateAlerte()).isEqualTo(AUJOURD_HUI);
        assertThat(cuivre.getDateAlerte()).isEqualTo(AUJOURD_HUI);
    }

    @Test
    @DisplayName("F16 : une compétence déjà signalée n'est pas alertée deux fois")
    void f16PasDeDoublon() {
        when(obtentionRepository.findAllByDateExpirationBetweenAndDateAlerteIsNull(
                AUJOURD_HUI, AUJOURD_HUI.plusDays(30))).thenReturn(List.of());

        assertThat(alerteService.genererAlertes()).isEmpty();
    }

    // ── Fabriques ──────────────────────────────────────────────────────────────

    private void prepareCreate() {
        Client client = client(7, "Dupont");
        TypeIntervention type = type(1, "RACCORDEMENT_FIBRE");
        com.interventionsterrain.interventions_terrain_api.entity.Adresse adresse =
                new com.interventionsterrain.interventions_terrain_api.entity.Adresse(
                        "12 rue de la Paix", "Paris");
        ReflectionTestUtils.setField(adresse, "idAdresse", 5);
        when(clientRepository.existsById(7)).thenReturn(true);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(technicienRepository.existsById(3)).thenReturn(true);
        when(interventionRepository.findChevauchements(3, DEBUT, FIN, -1)).thenReturn(List.of());
        when(exigenceRepository.findAllByIdIdType(1)).thenReturn(List.of());
        when(adresseRepository.save(any())).thenReturn(adresse);
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> {
            Intervention i = invocation.getArgument(0);
            ReflectionTestUtils.setField(i, "idIntervention", 20);
            return i;
        });
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            Affectation a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "idAffectation", 10);
            return a;
        });
        Statut planifiee = statut("PLANIFIEE");
        when(statutRepository.findByLibelle("PLANIFIEE")).thenReturn(Optional.of(planifiee));
    }

    private Agence agence(Integer id, String nom) {
        Agence agence = new Agence(nom, nom, "Paris", null);
        ReflectionTestUtils.setField(agence, "idAgence", id);
        return agence;
    }

    private Equipe equipe(Integer id, Integer idAgence) {
        Equipe equipe = new Equipe("Équipe " + id, idAgence);
        ReflectionTestUtils.setField(equipe, "idEquipe", id);
        return equipe;
    }

    private Technicien technicien(Integer id, String nom, Integer idEquipe) {
        Technicien technicien = new Technicien(nom, "Prenom", nom.toLowerCase() + "@example.test",
                "0102030405", idEquipe, "MAT" + id);
        ReflectionTestUtils.setField(technicien, "idTechnicien", id);
        return technicien;
    }

    private Competence competence(Integer id, String libelle) {
        Competence competence = new Competence(libelle, null);
        ReflectionTestUtils.setField(competence, "idCompetence", id);
        return competence;
    }

    private TypeIntervention type(Integer id, String code) {
        TypeIntervention type = mock(TypeIntervention.class);
        lenient().when(type.getIdType()).thenReturn(id);
        lenient().when(type.getCode()).thenReturn(code);
        return type;
    }

    private Client client(Integer id, String nom) {
        Client client = new Client(nom, "Marie", "client@example.test", "0102030405",
                "5 avenue du parc", "Paris");
        ReflectionTestUtils.setField(client, "idClient", id);
        return client;
    }

    private Intervention intervention(Integer id, LocalDateTime debut, LocalDateTime fin) {
        Intervention intervention = new Intervention(LocalDateTime.of(2026, 11, 24, 9, 0),
                debut, fin, 7, 50, "RACCORDEMENT_FIBRE");
        ReflectionTestUtils.setField(intervention, "idIntervention", id);
        return intervention;
    }

    private Affectation affectation(Integer id, Integer idIntervention, Integer idTechnicien) {
        Affectation affectation = new Affectation(idIntervention, idTechnicien, true);
        ReflectionTestUtils.setField(affectation, "idAffectation", id);
        return affectation;
    }

    private Statut statut(String libelle) {
        Statut statut = mock(Statut.class);
        lenient().when(statut.getLibelle()).thenReturn(libelle);
        return statut;
    }
}

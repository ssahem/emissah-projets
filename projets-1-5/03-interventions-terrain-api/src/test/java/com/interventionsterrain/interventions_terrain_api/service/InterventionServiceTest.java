package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.AddAffectationRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.CreateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateInterventionRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.InterventionResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import com.interventionsterrain.interventions_terrain_api.enums.StatutIntervention;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.mapper.AffectationMapper;
import com.interventionsterrain.interventions_terrain_api.mapper.InterventionMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TypeInterventionRepository;
import java.time.Clock;
import java.time.Instant;
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

/** Règles métier F5 : type résolu en service, adresse créée, responsable unique, statut initial (A-02, A-03, E-21). */
@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    private static final LocalDateTime DEMANDE = LocalDateTime.of(2026, 11, 24, 9, 0);
    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 1, 9, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 1, 12, 0);

    @Mock private InterventionRepository interventionRepository;
    @Mock private AdresseRepository adresseRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private TechnicienRepository technicienRepository;
    @Mock private TypeInterventionRepository typeRepository;
    @Mock private StatutRepository statutRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository exigenceRepository;
    @Mock private com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository obtentionRepository;
    @Mock private com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository competenceRepository;

    private InterventionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new InterventionService(interventionRepository, adresseRepository, clientRepository,
                technicienRepository, typeRepository, statutRepository, affectationRepository,
                historiqueRepository, exigenceRepository, obtentionRepository, competenceRepository,
                new InterventionMapper(), new AffectationMapper(), clock);
    }

    @Test
    @DisplayName("Un code de type inconnu est refusé avec un 400")
    void createRefuseTypeInconnu() {
        when(clientRepository.existsById(1)).thenReturn(true);
        when(typeRepository.findByCode("INCONNU")).thenReturn(Optional.empty());

        CreateInterventionRequest requete = new CreateInterventionRequest(1, "INCONNU", 3,
                DEMANDE, DEBUT, FIN, "12 rue de la Paix", "Paris");
        assertThatThrownBy(() -> service.create(requete))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("inconnu");
        verify(adresseRepository, never()).save(any());
    }

    @Test
    @DisplayName("La création persiste l'adresse, le responsable et le statut PLANIFIEE")
    void createPersisteAdresseEtResponsable() {
        prepareCreate();
        ArgumentCaptor<HistoriqueStatut> historique = ArgumentCaptor.forClass(HistoriqueStatut.class);

        InterventionResponse response = service.create(requeteCreate());

        assertThat(response.idIntervention()).isEqualTo(20);
        assertThat(response.voie()).isEqualTo("12 rue de la Paix");
        assertThat(response.commune()).isEqualTo("Paris");
        assertThat(response.idTechnicienResponsable()).isEqualTo(3);
        verify(adresseRepository).save(any(Adresse.class));
        verify(historiqueRepository).save(historique.capture());
        assertThat(historique.getValue().getIdStatut()).isEqualTo(1);
        assertThat(historique.getValue().getDateHeure()).isEqualTo(
                LocalDateTime.of(2026, 11, 24, 10, 0));
    }

    @Test
    @DisplayName("Le changement de responsable recopie le statut courant et libère l'ancien")
    void updateChangementResponsableRecopieStatut() {
        Intervention intervention = intervention();
        Affectation ancien = affectation(10, 1, true);
        Statut planifiee = statut("PLANIFIEE");
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention));
        when(technicienRepository.existsById(2)).thenReturn(true);
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(ancien));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(10))
                .thenReturn(Optional.of(new HistoriqueStatut(DEMANDE, 1, 10)));
        when(statutRepository.findById(1)).thenReturn(Optional.of(planifiee));
        when(statutRepository.findByLibelle("PLANIFIEE")).thenReturn(Optional.of(planifiee));
        TypeIntervention type = mock(TypeIntervention.class);
        when(type.getIdType()).thenReturn(1);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(affectationRepository.findByIdInterventionAndIdTechnicien(20, 2))
                .thenReturn(Optional.empty());
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            Affectation a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "idAffectation", 11);
            return a;
        });

        service.update(20, new UpdateInterventionRequest(DEBUT, FIN, 2));

        assertThat(ancien.isEstResponsable()).isFalse();
        verify(affectationRepository).flush();
        ArgumentCaptor<HistoriqueStatut> historique = ArgumentCaptor.forClass(HistoriqueStatut.class);
        verify(historiqueRepository).save(historique.capture());
        assertThat(historique.getValue().getIdAffectation()).isEqualTo(11);
        assertThat(historique.getValue().getIdStatut()).isEqualTo(1);
    }

    @Test
    @DisplayName("Une intervention commencée n'est plus modifiable")
    void updateRefuseSiStatutNonPlanifie() {
        Affectation responsable = affectation(10, 1, true);
        Statut enCours = statut("EN_COURS");
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention()));
        when(technicienRepository.existsById(2)).thenReturn(true);
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(historiqueRepository.findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(10))
                .thenReturn(Optional.of(new HistoriqueStatut(DEMANDE, 2, 10)));
        when(statutRepository.findById(2)).thenReturn(Optional.of(enCours));

        assertThatThrownBy(() -> service.update(20, new UpdateInterventionRequest(DEBUT, FIN, 2)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("PLANIFIEE");
    }

    @Test
    @DisplayName("Un créneau passé est refusé")
    void createRefuseCreneauPasse() {
        when(clientRepository.existsById(1)).thenReturn(true);
        TypeIntervention type = mock(TypeIntervention.class);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(technicienRepository.existsById(3)).thenReturn(true);
        LocalDateTime passe = LocalDateTime.of(2026, 11, 23, 9, 0);

        assertThatThrownBy(() -> service.create(new CreateInterventionRequest(1, "RACCORDEMENT_FIBRE",
                3, DEMANDE, passe, passe.plusHours(3), "12 rue de la Paix", "Paris")))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("futur");
    }

    @Test
    @DisplayName("Un technicien déjà affecté ne peut pas être ajouté deux fois")
    void addAffectationRefuseDoublon() {
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention()));
        when(technicienRepository.existsById(1)).thenReturn(true);
        when(affectationRepository.findByIdInterventionAndIdTechnicien(20, 1))
                .thenReturn(Optional.of(affectation(10, 1, true)));

        assertThatThrownBy(() -> service.addAffectation(20, new AddAffectationRequest(1, false)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà affecté");
        verify(affectationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un second responsable est refusé avec un 409")
    void addAffectationRefuseSecondResponsable() {
        Affectation responsable = affectation(10, 1, true);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention()));
        when(technicienRepository.existsById(2)).thenReturn(true);
        when(affectationRepository.findByIdInterventionAndIdTechnicien(20, 2)).thenReturn(Optional.empty());
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));

        assertThatThrownBy(() -> service.addAffectation(20, new AddAffectationRequest(2, true)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà un technicien responsable");
        verify(affectationRepository, never()).save(any());
    }

    @Test
    @DisplayName("La suppression nettoie historiques et affectations avant l'intervention")
    void deleteNettoieLesEnfants() {
        Intervention intervention = intervention();
        Affectation responsable = affectation(10, 1, true);
        when(interventionRepository.findById(20)).thenReturn(Optional.of(intervention));
        when(affectationRepository.findAllByIdIntervention(20)).thenReturn(List.of(responsable));

        service.delete(20);

        verify(historiqueRepository).deleteByIdAffectation(10);
        verify(affectationRepository).deleteByIdIntervention(20);
        verify(interventionRepository).delete(intervention);
    }

    /** Prépare les mocks nécessaires à une création nominale. */
    private void prepareCreate() {
        TypeIntervention type = mock(TypeIntervention.class);
        when(type.getCode()).thenReturn("RACCORDEMENT_FIBRE");
        Adresse adresse = new Adresse("12 rue de la Paix", "Paris");
        ReflectionTestUtils.setField(adresse, "idAdresse", 5);
        Affectation responsable = new Affectation(20, 3, true);
        ReflectionTestUtils.setField(responsable, "idAffectation", 10);
        Statut planifiee = statut("PLANIFIEE");

        when(clientRepository.existsById(1)).thenReturn(true);
        when(typeRepository.findByCode("RACCORDEMENT_FIBRE")).thenReturn(Optional.of(type));
        when(technicienRepository.existsById(3)).thenReturn(true);
        when(adresseRepository.save(any(Adresse.class))).thenReturn(adresse);
        when(adresseRepository.findById(5)).thenReturn(Optional.of(adresse));
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> {
            Intervention i = invocation.getArgument(0);
            ReflectionTestUtils.setField(i, "idIntervention", 20);
            return i;
        });
        when(affectationRepository.save(any(Affectation.class))).thenReturn(responsable);
        when(affectationRepository.findByIdInterventionAndEstResponsableTrue(20))
                .thenReturn(Optional.of(responsable));
        when(statutRepository.findByLibelle("PLANIFIEE")).thenReturn(Optional.of(planifiee));
    }

    /** Corps de création nominal. */
    private CreateInterventionRequest requeteCreate() {
        return new CreateInterventionRequest(1, "RACCORDEMENT_FIBRE", 3,
                DEMANDE, DEBUT, FIN, "12 rue de la Paix", "Paris");
    }

    /** Intervention avec son identifiant. */
    private Intervention intervention() {
        Intervention intervention = new Intervention(DEMANDE, DEBUT, FIN, 1, 5, "RACCORDEMENT_FIBRE");
        ReflectionTestUtils.setField(intervention, "idIntervention", 20);
        return intervention;
    }

    /** Affectation avec son identifiant. */
    private Affectation affectation(Integer id, Integer idTechnicien, boolean responsable) {
        Affectation affectation = new Affectation(20, idTechnicien, responsable);
        ReflectionTestUtils.setField(affectation, "idAffectation", id);
        return affectation;
    }

    /** Statut simulé. */
    private Statut statut(String libelle) {
        Statut statut = mock(Statut.class);
        lenient().when(statut.getLibelle()).thenReturn(libelle);
        lenient().when(statut.getIdStatut()).thenReturn(libelle.equals("PLANIFIEE") ? 1 : 3);
        return statut;
    }
}

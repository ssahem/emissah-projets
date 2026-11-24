package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateCompetenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.CompetenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.mapper.CompetenceMapper;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ExigenceCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Règles métier F3. */
@ExtendWith(MockitoExtension.class)
class CompetenceServiceTest {

    @Mock private CompetenceRepository competenceRepository;
    @Mock private ObtentionCompetenceRepository obtentionRepository;
    @Mock private ExigenceCompetenceRepository exigenceRepository;

    private CompetenceService service;

    @BeforeEach
    void setUp() {
        service = new CompetenceService(competenceRepository, obtentionRepository,
                exigenceRepository, new CompetenceMapper());
    }

    @Test
    @DisplayName("Un libellé déjà utilisé est refusé")
    void createRefuseLibelleDuplique() {
        Competence existante = competence(1, "Fibre");
        when(competenceRepository.findByLibelle("Fibre")).thenReturn(Optional.of(existante));

        assertThatThrownBy(() -> service.create(new CreateCompetenceRequest("Fibre", null)))
                .isInstanceOf(ConflitMetierException.class);
        verify(competenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("La description est modifiable")
    void updateModifieDescription() {
        Competence competence = competence(1, "Fibre");
        when(competenceRepository.findById(1)).thenReturn(Optional.of(competence));
        when(competenceRepository.findByLibelle("Fibre")).thenReturn(Optional.of(competence));

        CompetenceResponse response = service.update(1,
                new UpdateCompetenceRequest("Fibre", "Raccordement fibre optique"));

        assertThat(response.description()).isEqualTo("Raccordement fibre optique");
    }

    @Test
    @DisplayName("Une compétence obtenue par un technicien ne peut pas être supprimée")
    void deleteRefuseSiObtention() {
        Competence competence = competence(1, "Fibre");
        when(competenceRepository.findById(1)).thenReturn(Optional.of(competence));
        when(obtentionRepository.existsByIdCompetence(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("obtenue");
        verify(competenceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Une compétence exigée par un type ne peut pas être supprimée")
    void deleteRefuseSiExigence() {
        Competence competence = competence(1, "Fibre");
        when(competenceRepository.findById(1)).thenReturn(Optional.of(competence));
        when(exigenceRepository.existsByIdIdCompetence(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class);
        verify(competenceRepository, never()).delete(any());
    }

    /** Construit une compétence avec son identifiant. */
    private Competence competence(Integer id, String libelle) {
        Competence competence = new Competence(libelle, null);
        ReflectionTestUtils.setField(competence, "idCompetence", id);
        return competence;
    }
}

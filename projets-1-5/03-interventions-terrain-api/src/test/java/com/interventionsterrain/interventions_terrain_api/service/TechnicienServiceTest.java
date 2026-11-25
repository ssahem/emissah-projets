package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateTechnicienRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.TechnicienResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import com.interventionsterrain.interventions_terrain_api.entity.Technicien;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.exception.DonneeInvalideException;
import com.interventionsterrain.interventions_terrain_api.mapper.EquipeLookup;
import com.interventionsterrain.interventions_terrain_api.mapper.TechnicienMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AdresseRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.CompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Règles métier F2 et résolution de l'agence via l'équipe (A-01).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F2 et résolution de l'agence via l'équipe (A-01).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class TechnicienServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

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
    @Mock private Clock clock;

    private TechnicienService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        TechnicienMapper mapper = new TechnicienMapper(new EquipeLookup(equipeRepository, agenceRepository));
        service = new TechnicienService(technicienRepository, equipeRepository, agenceRepository,
                affectationRepository, obtentionRepository, competenceRepository, interventionRepository,
                adresseRepository, clientRepository, historiqueRepository, statutRepository, mapper, clock);
    }

    // ── createRefuseMatriculeDuplique() ──────────────────────────────────────

    @Test
    @DisplayName("Un matricule déjà utilisé est refusé avec un 409")
    void createRefuseMatriculeDuplique() {
        when(technicienRepository.existsByMatricule("MAT001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateTechnicienRequest(
                "Martin", "Camille", null, "0102030405", 1, null, "MAT001")))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("matricule");
        verify(technicienRepository, never()).save(any());
    }

    // ── createCreeEquipeParDefaut() ──────────────────────────────────────────

    @Test
    @DisplayName("Sans équipe précisée, une équipe de l'agence est créée et l'agence est déduite")
    void createCreeEquipeParDefaut() {
        Agence agence = agence(1, "Paris Centre");
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence));
        when(equipeRepository.findFirstByIdAgenceOrderByIdEquipeAsc(1)).thenReturn(Optional.empty());
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> {
            Equipe e = invocation.getArgument(0);
            ReflectionTestUtils.setField(e, "idEquipe", 9);
            return e;
        });
        when(technicienRepository.save(any(Technicien.class))).thenAnswer(invocation -> {
            Technicien t = invocation.getArgument(0);
            ReflectionTestUtils.setField(t, "idTechnicien", 5);
            return t;
        });
        when(equipeRepository.findById(9)).thenReturn(Optional.of(
                equipe(9, 1, agence)));

        TechnicienResponse response = service.create(new CreateTechnicienRequest(
                "Martin", "Camille", "camille@example.test", "0102030405", 1, null, "MAT001"));

        assertThat(response.idTechnicien()).isEqualTo(5);
        assertThat(response.idAgence()).isEqualTo(1);
        assertThat(response.nomAgence()).isEqualTo("Paris Centre");
        assertThat(response.email()).isEqualTo("camille@example.test");
    }

    // ── createRefuseEquipeHorsAgence() ───────────────────────────────────────

    @Test
    @DisplayName("Une équipe d'une autre agence est refusée avec un 400")
    void createRefuseEquipeHorsAgence() {
        when(agenceRepository.findById(1)).thenReturn(Optional.of(agence(1, "Paris Centre")));
        when(equipeRepository.existsByIdEquipeAndIdAgence(8, 1)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateTechnicienRequest(
                "Martin", "Camille", "c@example.test", "0102030405", 1, 8, "MAT001")))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("n'appartient pas");
    }

    // ── updateRefuseMatriculeModifie() ───────────────────────────────────────

    @Test
    @DisplayName("Le matricule est figé en modification")
    void updateRefuseMatriculeModifie() {
        Technicien technicien = new Technicien("Martin", "Camille", "c@example.test",
                "0102030405", 9, "MAT001");
        when(technicienRepository.findById(5)).thenReturn(Optional.of(technicien));

        assertThatThrownBy(() -> service.update(5, new UpdateTechnicienRequest(
                "Martin", "Camille", "c@example.test", "0102030405", 1, 9, "MAT999")))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("ne peut pas être modifié");
    }

    // ── deleteRefuseSiAffectation() ──────────────────────────────────────────

    @Test
    @DisplayName("La suppression est refusée si une affectation existe")
    void deleteRefuseSiAffectation() {
        Technicien technicien = new Technicien("Martin", "Camille", "c@example.test",
                "0102030405", 9, "MAT001");
        when(technicienRepository.findById(5)).thenReturn(Optional.of(technicien));
        when(affectationRepository.existsByIdTechnicien(5)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("affecté");
        verify(technicienRepository, never()).delete(any());
    }


    // ── agence() ─────────────────────────────────────────────────────────────

    /**
     * Construit une agence avec son identifiant.
     */

    private Agence agence(Integer id, String nom) {
        Agence agence = new Agence(nom, nom, nom, null);
        ReflectionTestUtils.setField(agence, "idAgence", id);
        return agence;
    }


    // ── equipe() ─────────────────────────────────────────────────────────────

    /**
     * Construit une équipe avec son identifiant.
     */

    private Equipe equipe(Integer id, Integer idAgence, Agence agence) {
        Equipe equipe = new Equipe("Équipe " + agence.getNom(), idAgence);
        ReflectionTestUtils.setField(equipe, "idEquipe", id);
        return equipe;
    }
}

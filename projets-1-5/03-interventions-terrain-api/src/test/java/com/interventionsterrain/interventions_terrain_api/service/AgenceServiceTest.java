package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateAgenceRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.AgenceResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.mapper.AgenceMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AgenceRepository;
import com.interventionsterrain.interventions_terrain_api.repository.EquipeRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ObtentionCompetenceRepository;
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
 * Règles métier F1.
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F1.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class AgenceServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private AgenceRepository agenceRepository;
    @Mock private EquipeRepository equipeRepository;
    @Mock private TechnicienRepository technicienRepository;
    @Mock private ObtentionCompetenceRepository obtentionRepository;
    @Mock private Clock clock;

    private AgenceService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new AgenceService(agenceRepository, equipeRepository, technicienRepository,
                obtentionRepository, new AgenceMapper(), clock);
    }

    // ── createRefuseNomDuplique() ────────────────────────────────────────────

    @Test
    @DisplayName("La création refuse un nom déjà utilisé avec un 409")
    void createRefuseNomDuplique() {
        when(agenceRepository.findByNom("Paris Centre"))
                .thenReturn(Optional.of(agence(1, "Paris Centre", "Paris")));

        assertThatThrownBy(() -> service.create(
                new CreateAgenceRequest("Paris Centre", "Paris", "1 rue de Rivoli", null)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà utilisé");
        verify(agenceRepository, never()).save(any());
    }

    // ── createAvecAdresseParDefaut() ─────────────────────────────────────────

    @Test
    @DisplayName("La création utilise la ville comme adresse par défaut")
    void createAvecAdresseParDefaut() {
        when(agenceRepository.findByNom("Lyon Est")).thenReturn(Optional.empty());
        when(agenceRepository.save(any(Agence.class))).thenAnswer(invocation -> {
            Agence a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "idAgence", 7);
            return a;
        });

        AgenceResponse response = service.create(new CreateAgenceRequest("Lyon Est", "Lyon", null, null));

        assertThat(response.idAgence()).isEqualTo(7);
        assertThat(response.adresse()).isEqualTo("Lyon");
    }

    // ── updateRefuseChangementDeVilleSiEquipe() ──────────────────────────────

    @Test
    @DisplayName("La ville est figée dès qu'une équipe est rattachée")
    void updateRefuseChangementDeVilleSiEquipe() {
        Agence agence = agence(3, "Paris Centre", "Paris");
        when(agenceRepository.findById(3)).thenReturn(Optional.of(agence));
        when(equipeRepository.existsByIdAgence(3)).thenReturn(true);

        assertThatThrownBy(() -> service.update(3, new UpdateAgenceRequest("Paris Centre", "Lyon", null)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("ne peut plus changer");
    }

    // ── updateResponsableAutorise() ──────────────────────────────────────────

    @Test
    @DisplayName("Le responsable reste modifiable même si la ville est figée")
    void updateResponsableAutorise() {
        Agence agence = agence(3, "Paris Centre", "Paris");
        when(agenceRepository.findById(3)).thenReturn(Optional.of(agence));
        when(equipeRepository.existsByIdAgence(3)).thenReturn(true);

        AgenceResponse response = service.update(3,
                new UpdateAgenceRequest("Paris Centre", "Paris", "Mme Durand"));

        assertThat(response.responsable()).isEqualTo("Mme Durand");
    }

    // ── deleteRefuseSiEquipes() ──────────────────────────────────────────────

    @Test
    @DisplayName("La suppression est refusée si des équipes existent")
    void deleteRefuseSiEquipes() {
        Agence agence = agence(3, "Paris Centre", "Paris");
        when(agenceRepository.findById(3)).thenReturn(Optional.of(agence));
        when(equipeRepository.existsByIdAgence(3)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(3))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("équipes");
        verify(agenceRepository, never()).delete(any());
    }

    /** Construit une agence avec son identifiant. */

    // ── agence() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour agence().
     */

    private Agence agence(Integer id, String nom, String ville) {
        Agence agence = new Agence(nom, ville, ville, null);
        ReflectionTestUtils.setField(agence, "idAgence", id);
        return agence;
    }
}

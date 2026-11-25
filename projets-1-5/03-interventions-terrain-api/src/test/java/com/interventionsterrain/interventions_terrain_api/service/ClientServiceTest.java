package com.interventionsterrain.interventions_terrain_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interventionsterrain.interventions_terrain_api.dto.request.CreateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.request.UpdateClientRequest;
import com.interventionsterrain.interventions_terrain_api.dto.response.ClientResponse;
import com.interventionsterrain.interventions_terrain_api.entity.Client;
import com.interventionsterrain.interventions_terrain_api.exception.ConflitMetierException;
import com.interventionsterrain.interventions_terrain_api.mapper.ClientMapper;
import com.interventionsterrain.interventions_terrain_api.repository.AffectationRepository;
import com.interventionsterrain.interventions_terrain_api.repository.ClientRepository;
import com.interventionsterrain.interventions_terrain_api.repository.HistoriqueStatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.InterventionRepository;
import com.interventionsterrain.interventions_terrain_api.repository.StatutRepository;
import com.interventionsterrain.interventions_terrain_api.repository.TechnicienRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Règles métier F4 (adresse texte obligatoire, email unique).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F4 (adresse texte obligatoire, email unique).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ClientRepository clientRepository;
    @Mock private InterventionRepository interventionRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private TechnicienRepository technicienRepository;
    @Mock private HistoriqueStatutRepository historiqueRepository;
    @Mock private StatutRepository statutRepository;

    private ClientService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new ClientService(clientRepository, interventionRepository, affectationRepository,
                technicienRepository, historiqueRepository, statutRepository, new ClientMapper());
    }

    // ── createRefuseEmailDuplique() ──────────────────────────────────────────

    @Test
    @DisplayName("Un email déjà utilisé est refusé à la création")
    void createRefuseEmailDuplique() {
        when(clientRepository.existsByEmail("marie@example.test")).thenReturn(true);

        // Le contrôle de création ne consulte aucun identifiant courant : le conflit doit être levé.
        assertThatThrownBy(() -> service.create(new CreateClientRequest(
                "Dupont", "Marie", "marie@example.test", "0102030407", "5 avenue du parc", "Paris")))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("email");
        verify(clientRepository, never()).save(any());
    }

    // ── createConserveAdresse() ──────────────────────────────────────────────

    @Test
    @DisplayName("La création conserve l'adresse et la commune textuelles")
    void createConserveAdresse() {
        when(clientRepository.existsByEmail("marie@example.test")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "idClient", 2);
            return c;
        });

        ClientResponse response = service.create(new CreateClientRequest(
                "Dupont", "Marie", "marie@example.test", "0102030407", "5 avenue du parc", "Paris"));

        assertThat(response.adresse()).isEqualTo("5 avenue du parc");
        assertThat(response.commune()).isEqualTo("Paris");
        assertThat(response.telephone()).isEqualTo("0102030407");
    }

    // ── updateAccepteEmailInchange() ─────────────────────────────────────────

    @Test
    @DisplayName("Une modification peut conserver le même email")
    void updateAccepteEmailInchange() {
        Client client = client(1, "marie@example.test");
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(clientRepository.existsByEmail("marie@example.test")).thenReturn(true);

        ClientResponse response = service.update(1, new UpdateClientRequest(
                "Dupont", "Marie", "marie@example.test", "0102030407", "6 rue Neuve", "Lyon"));

        assertThat(response.email()).isEqualTo("marie@example.test");
        assertThat(response.adresse()).isEqualTo("6 rue Neuve");
    }

    // ── deleteRefuseSiInterventions() ────────────────────────────────────────

    @Test
    @DisplayName("Un client avec des interventions ne peut pas être supprimé")
    void deleteRefuseSiInterventions() {
        Client client = client(1, "marie@example.test");
        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(interventionRepository.existsByIdClient(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("interventions");
        verify(clientRepository, never()).delete(any());
    }


    // ── client() ─────────────────────────────────────────────────────────────

    /**
     * Construit un client avec son identifiant.
     */

    private Client client(Integer id, String email) {
        Client client = new Client("Dupont", "Marie", email, "0102030407", "5 avenue du parc", "Paris");
        ReflectionTestUtils.setField(client, "idClient", id);
        return client;
    }
}

package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.mapper.MatiereMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import java.math.BigDecimal;
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
 * Règles métier F3 (matières).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F3 (matières).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class MatiereServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private MatiereRepository matiereRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private TrimestreRepository trimestreRepository;

    private MatiereService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new MatiereService(matiereRepository, enseignementRepository,
                trimestreRepository, new MatiereMapper());
    }

    // ── findAll() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("La liste des matières est triée par libellé")
    void findAll() {
        when(matiereRepository.findAllByOrderByLibelleAsc())
                .thenReturn(List.of(new Matiere("Anglais", "ANGL", BigDecimal.TWO)));

        assertThat(service.findAll()).singleElement().satisfies(
                m -> assertThat(m.libelle()).isEqualTo("Anglais"));
    }

    // ── createRefuseDoublon() ────────────────────────────────────────────────

    @Test
    @DisplayName("La création refuse un libellé déjà utilisé")
    void createRefuseDoublon() {
        when(matiereRepository.findByLibelle("Mathematiques"))
                .thenReturn(Optional.of(new Matiere("Mathematiques", "MATH", BigDecimal.ONE)));

        assertThatThrownBy(() -> service.create(
                new CreateMatiereRequest("Mathematiques", "MA2", BigDecimal.ONE)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà utilisé");
        verify(matiereRepository, never()).save(any());
    }

    // ── createNominal() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("La création persiste une matière avec son coefficient")
    void createNominal() {
        when(matiereRepository.findByLibelle("Physique")).thenReturn(Optional.empty());
        when(matiereRepository.save(any(Matiere.class))).thenAnswer(invocation -> {
            Matiere m = invocation.getArgument(0);
            ReflectionTestUtils.setField(m, "idMatiere", 5);
            return m;
        });

        assertThat(service.create(new CreateMatiereRequest("Physique", "PHYS", BigDecimal.valueOf(3)))
                .coefficient()).isEqualByComparingTo("3");
    }

    // ── updateRefuseApresCloture() ───────────────────────────────────────────

    @Test
    @DisplayName("Le coefficient est figé dès qu'un trimestre est clôturé")
    void updateRefuseApresCloture() {
        Matiere matiere = matiere(1);
        when(matiereRepository.findById(1)).thenReturn(Optional.of(matiere));
        when(matiereRepository.findByLibelle("Mathematiques")).thenReturn(Optional.of(matiere));
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(true);

        assertThatThrownBy(() -> service.update(1,
                new UpdateMatiereRequest("Mathematiques", BigDecimal.TEN)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("clôturé");
    }

    // ── updateNominal() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Le coefficient est modifiable avant toute clôture")
    void updateNominal() {
        Matiere matiere = matiere(1);
        when(matiereRepository.findById(1)).thenReturn(Optional.of(matiere));
        when(matiereRepository.findByLibelle("Mathematiques")).thenReturn(Optional.of(matiere));
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(false);

        assertThat(service.update(1, new UpdateMatiereRequest("Mathematiques", BigDecimal.TEN))
                .coefficient()).isEqualByComparingTo("10");
    }


    // ── matiere() ────────────────────────────────────────────────────────────

    /**
     * Construit une matière avec son identifiant.
     */

    private Matiere matiere(Integer id) {
        Matiere m = new Matiere("Mathematiques", "MATH", BigDecimal.ONE);
        ReflectionTestUtils.setField(m, "idMatiere", id);
        return m;
    }

    // ── deleteRefuseMatiereAffectee() ────────────────────────────────────────

    @Test
    @DisplayName("La suppression est refusée si la matière est enseignée")
    void deleteRefuseMatiereAffectee() {
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(new Matiere("Mathematiques", "MATH", BigDecimal.ONE)));
        when(enseignementRepository.existsByIdMatiere(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("professeur affecté");
        verify(matiereRepository, never()).delete(any());
    }
}

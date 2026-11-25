package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.ClasseMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
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
 * Règles métier F1 (classes).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F1 (classes).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ClasseServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ClasseRepository classeRepository;
    @Mock private InscriptionRepository inscriptionRepository;

    private ClasseService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new ClasseService(classeRepository, inscriptionRepository,
                new ClasseMapper(inscriptionRepository));
    }

    // ── findAllAvecEffectif() ────────────────────────────────────────────────

    @Test
    @DisplayName("La liste renvoie les classes avec leur effectif courant")
    void findAllAvecEffectif() {
        when(classeRepository.findAllByOrderByIdNiveauAscNomAsc()).thenReturn(List.of(classe(1, "6A")));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(3L);

        assertThat(service.findAll()).singleElement()
                .satisfies(c -> {
                    assertThat(c.nom()).isEqualTo("6A");
                    assertThat(c.effectifActuel()).isEqualTo(3L);
                });
    }

    // ── findByIdIntrouvable() ────────────────────────────────────────────────

    @Test
    @DisplayName("Un identifiant inconnu renvoie une erreur 404 métier")
    void findByIdIntrouvable() {
        when(classeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessageContaining("99");
    }

    // ── createRefuseDoublon() ────────────────────────────────────────────────

    @Test
    @DisplayName("La création refuse un nom déjà utilisé dans le même niveau")
    void createRefuseDoublon() {
        when(classeRepository.findByNomAndIdNiveau("6A", 1)).thenReturn(Optional.of(classe(1, "6A")));

        assertThatThrownBy(() -> service.create(new CreateClasseRequest("6A", 2, 1, 30)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("existe déjà");
        verify(classeRepository, never()).save(any());
    }

    // ── createNominal() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("La création persiste une classe valide")
    void createNominal() {
        when(classeRepository.findByNomAndIdNiveau("5B", 2)).thenReturn(Optional.empty());
        when(classeRepository.save(any(Classe.class))).thenAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "idClasse", 8);
            return c;
        });
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(8)).thenReturn(0L);

        assertThat(service.create(new CreateClasseRequest("5B", 2, 2, 28)).idClasse()).isEqualTo(8);
    }

    // ── updateRefuseEffectifInsuffisant() ────────────────────────────────────

    @Test
    @DisplayName("L'effectif maximal ne peut pas descendre sous l'effectif actuel")
    void updateRefuseEffectifInsuffisant() {
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A")));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(4L);

        assertThatThrownBy(() -> service.update(1, new UpdateClasseRequest(3)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("effectif actuel");
    }

    // ── deleteRefuseClasseOccupee() ──────────────────────────────────────────

    @Test
    @DisplayName("La suppression est refusée si des élèves sont inscrits")
    void deleteRefuseClasseOccupee() {
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A")));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(2L);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("élèves");
        verify(classeRepository, never()).delete(any());
    }

    // ── deleteClasseVide() ───────────────────────────────────────────────────

    @Test
    @DisplayName("Une classe vide peut être supprimée")
    void deleteClasseVide() {
        Classe classe = classe(1, "6A");
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(0L);

        service.delete(1);

        verify(classeRepository).delete(classe);
    }


    // ── classe() ─────────────────────────────────────────────────────────────

    /**
     * Construit une classe avec son identifiant.
     */

    private Classe classe(Integer id, String nom) {
        Classe classe = new Classe(nom, 2, 1, 30);
        ReflectionTestUtils.setField(classe, "idClasse", id);
        return classe;
    }
}

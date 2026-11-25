package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSalarieRequest;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.AffectationMapper;
import com.flottevehicules.flotte_vehicules_api.mapper.SalarieMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SalarieRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
 * Tests unitaires de F3 et F7.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de F3 et F7.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class SalarieServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private SalarieRepository salarieRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private VehiculeRepository vehiculeRepository;

    private SalarieService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        AffectationMapper affectationMapper = new AffectationMapper(vehiculeRepository, salarieRepository, clock);
        service = new SalarieService(salarieRepository, affectationRepository,
                new SalarieMapper(), affectationMapper);
    }

    // ── salarie() ────────────────────────────────────────────────────────────

    /**
     * Prépare un salarié de test avec des valeurs nominales.
     */

    private Salarie salarie(Integer id, String nom) {
        Salarie salarie = new Salarie(nom, "Alice", "alice@example.test", "MAT" + id, "Direction");
        ReflectionTestUtils.setField(salarie, "idSalarie", id);
        return salarie;
    }

    // ── findAllTriee() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : la liste est triée par nom")
    void findAllTriee() {
        when(salarieRepository.findAllByOrderByNomAsc())
                .thenReturn(List.of(salarie(1, "Bernard"), salarie(2, "Martin")));

        assertThat(service.findAll()).extracting("nom").containsExactly("Bernard", "Martin");
    }

    // ── findByIdPresent() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : un salarié existant est retourné")
    void findByIdPresent() {
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1, "Martin")));

        assertThat(service.findById(1).matricule()).isEqualTo("MAT1");
    }

    // ── findByIdAbsent() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : un identifiant inconnu renvoie 404")
    void findByIdAbsent() {
        when(salarieRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── createOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : la création enregistre le matricule et le service")
    void createOk() {
        when(salarieRepository.findByMatricule("MAT009")).thenReturn(Optional.empty());
        when(salarieRepository.findByEmail("alice@example.test")).thenReturn(Optional.empty());
        when(salarieRepository.save(any(Salarie.class))).thenAnswer(invocation -> {
            Salarie salarie = invocation.getArgument(0);
            ReflectionTestUtils.setField(salarie, "idSalarie", 9);
            return salarie;
        });

        var resultat = service.create(new CreateSalarieRequest(
                "Martin", "Alice", "alice@example.test", "MAT009", "Technique"));

        assertThat(resultat.idSalarie()).isEqualTo(9);
        assertThat(resultat.service()).isEqualTo("Technique");
    }

    // ── createMatriculeDoublon() ─────────────────────────────────────────────

    @Test
    @DisplayName("F3 : un matricule déjà utilisé est refusé")
    void createMatriculeDoublon() {
        when(salarieRepository.findByMatricule("MAT1"))
                .thenReturn(Optional.of(salarie(1, "Martin")));

        assertThatThrownBy(() -> service.create(new CreateSalarieRequest(
                "Martin", "Alice", "alice@example.test", "MAT1", "Technique")))
                .isInstanceOf(ConflitMetierException.class);
        verify(salarieRepository, never()).save(any());
    }

    // ── createEmailDoublon() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : une adresse électronique déjà utilisée est refusée")
    void createEmailDoublon() {
        when(salarieRepository.findByMatricule("MAT009")).thenReturn(Optional.empty());
        when(salarieRepository.findByEmail("alice@example.test"))
                .thenReturn(Optional.of(salarie(1, "Martin")));

        assertThatThrownBy(() -> service.create(new CreateSalarieRequest(
                "Martin", "Alice", "alice@example.test", "MAT009", "Technique")))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── updateGardeMatricule() ───────────────────────────────────────────────

    @Test
    @DisplayName("F3 : la modification ne change pas le matricule")
    void updateGardeMatricule() {
        Salarie salarie = salarie(1, "Martin");
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie));
        when(salarieRepository.findByEmail("nouveau@example.test")).thenReturn(Optional.empty());
        when(salarieRepository.save(salarie)).thenReturn(salarie);

        var resultat = service.update(1, new UpdateSalarieRequest(
                "Martin", "Alice", "nouveau@example.test", "Logistique"));

        assertThat(resultat.matricule()).isEqualTo("MAT1");
        assertThat(resultat.service()).isEqualTo("Logistique");
    }

    // ── deleteOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F3 : la suppression est autorisée sans affectation en cours")
    void deleteOk() {
        Salarie salarie = salarie(1, "Martin");
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie));

        service.delete(1);

        verify(salarieRepository).delete(salarie);
    }

    // ── deleteAvecAffectationEnCours() ───────────────────────────────────────

    @Test
    @DisplayName("F3 : la suppression est refusée avec une affectation en cours")
    void deleteAvecAffectationEnCours() {
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1, "Martin")));
        when(affectationRepository.existsByIdSalarieAndDateFinIsNull(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
        verify(salarieRepository, never()).delete(any());
    }

    // ── affectationsTriees() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F7 : les affectations sont retournées du plus récent au plus ancien")
    void affectationsTriees() {
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1, "Martin")));
        when(affectationRepository.findAllByIdSalarieOrderByDateDebutDesc(1)).thenReturn(List.of(
                new Affectation(LocalDate.parse("2026-11-01"), null, 2, 1),
                new Affectation(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"), 1, 1)));

        var resultat = service.affectations(1);

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).enCours()).isTrue();
        assertThat(resultat.get(1).enCours()).isFalse();
    }
}

package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.CreateRestitutionRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateAffectationRequest;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.AffectationMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SalarieRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VueRepository;
import java.math.BigDecimal;
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
 * Tests unitaires de F4, F11, F12, F13 et F14.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de F4, F11, F12, F13 et F14.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class AffectationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDate AUJOURD_HUI = LocalDate.parse("2026-11-24");

    @Mock private AffectationRepository affectationRepository;
    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private SalarieRepository salarieRepository;
    @Mock private EntretienRepository entretienRepository;
    @Mock private VueRepository vueRepository;

    private AffectationService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new AffectationService(affectationRepository, vehiculeRepository,
                salarieRepository, entretienRepository, vueRepository,
                new AffectationMapper(vehiculeRepository, salarieRepository, clock), clock);
    }

    // ── vehicule() ───────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour vehicule().
     */

    private Vehicule vehicule(Integer id, LocalDate miseEnService) {
        Vehicule vehicule = new Vehicule("AB-123-CD", "Peugeot", "308", miseEnService,
                1, "PROPRIETE", false, BigDecimal.TEN);
        ReflectionTestUtils.setField(vehicule, "idVehicule", id);
        return vehicule;
    }

    // ── salarie() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour salarie().
     */

    private Salarie salarie(Integer id) {
        Salarie salarie = new Salarie("Martin", "Alice", "alice@example.test", "MAT001", "Direction");
        ReflectionTestUtils.setField(salarie, "idSalarie", id);
        return salarie;
    }

    // ── affectation() ────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour affectation().
     */

    private Affectation affectation(Integer id, LocalDate debut, LocalDate fin, Integer km) {
        Affectation affectation = new Affectation(debut, fin, 1, 1);
        ReflectionTestUtils.setField(affectation, "idAffectation", id);
        if (km != null) {
            ReflectionTestUtils.setField(affectation, "kmRestitution", km);
        }
        return affectation;
    }

    // ── findAllTriee() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : la liste est triée par date de début décroissante")
    void findAllTriee() {
        when(affectationRepository.findAllByOrderByDateDebutDesc()).thenReturn(List.of(
                affectation(2, LocalDate.parse("2026-11-01"), null, null),
                affectation(1, LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"), 18500)));

        assertThat(service.findAll()).extracting("idAffectation").containsExactly(2, 1);
    }

    // ── findByIdPresent() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : une affectation existante est retournée")
    void findByIdPresent() {
        when(affectationRepository.findById(1))
                .thenReturn(Optional.of(affectation(1, AUJOURD_HUI, null, null)));

        assertThat(service.findById(1).idAffectation()).isEqualTo(1);
    }

    // ── findByIdAbsent() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : un identifiant inconnu renvoie 404")
    void findByIdAbsent() {
        when(affectationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── createOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : la création enregistre une affectation valide")
    void createOk() {
        when(vehiculeRepository.findById(1))
                .thenReturn(Optional.of(vehicule(1, LocalDate.parse("2024-01-15"))));
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1)));
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            Affectation affectation = invocation.getArgument(0);
            ReflectionTestUtils.setField(affectation, "idAffectation", 3);
            return affectation;
        });

        var resultat = service.create(new CreateAffectationRequest(
                1, 1, LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30")));

        assertThat(resultat.idAffectation()).isEqualTo(3);
        assertThat(resultat.dateDebut()).isEqualTo(LocalDate.parse("2026-11-01"));
    }

    // ── createAvantMiseEnService() ───────────────────────────────────────────

    @Test
    @DisplayName("F4 : une date avant la mise en service est refusée")
    void createAvantMiseEnService() {
        when(vehiculeRepository.findById(1))
                .thenReturn(Optional.of(vehicule(1, LocalDate.parse("2025-01-01"))));
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1)));

        assertThatThrownBy(() -> service.create(new CreateAffectationRequest(
                1, 1, LocalDate.parse("2024-01-01"), null)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── createChevauchement() ────────────────────────────────────────────────

    @Test
    @DisplayName("F11 : un chevauchement d'affectation est refusé en 409")
    void createChevauchement() {
        when(vehiculeRepository.findById(1))
                .thenReturn(Optional.of(vehicule(1, LocalDate.parse("2024-01-15"))));
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1)));
        Affectation bloquante = affectation(7, LocalDate.parse("2026-11-01"), null, null);
        when(affectationRepository.findChevauchements(1,
                LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-20")))
                .thenReturn(List.of(bloquante));

        assertThatThrownBy(() -> service.create(new CreateAffectationRequest(
                1, 1, LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-20"))))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── createImmobilisee() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F12 : une immobilisation est refusée en 409")
    void createImmobilisee() {
        when(vehiculeRepository.findById(1))
                .thenReturn(Optional.of(vehicule(1, LocalDate.parse("2024-01-15"))));
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1)));
        when(entretienRepository.findImmobilisationsChevauchant(1,
                LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-20")))
                .thenReturn(List.of(new Entretien(LocalDate.parse("2026-11-12"), null,
                        BigDecimal.TEN, 12000, 1, 1, 1)));

        assertThatThrownBy(() -> service.create(new CreateAffectationRequest(
                1, 1, LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-20"))))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── createDatesInversees() ───────────────────────────────────────────────

    @Test
    @DisplayName("F4 : des dates inversées sont refusées")
    void createDatesInversees() {
        when(vehiculeRepository.findById(1))
                .thenReturn(Optional.of(vehicule(1, LocalDate.parse("2024-01-15"))));
        when(salarieRepository.findById(1)).thenReturn(Optional.of(salarie(1)));

        assertThatThrownBy(() -> service.create(new CreateAffectationRequest(
                1, 1, LocalDate.parse("2026-11-20"), LocalDate.parse("2026-11-10"))))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── updateApresDebut() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : véhicule et salarié sont figés après le début")
    void updateApresDebut() {
        Affectation existante = affectation(1, LocalDate.parse("2026-11-01"), null, null);
        when(affectationRepository.findById(1)).thenReturn(Optional.of(existante));

        assertThatThrownBy(() -> service.update(1, new UpdateAffectationRequest(null, 2, null)))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── updateDateFin() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : la date de fin peut être modifiée")
    void updateDateFin() {
        Affectation existante = affectation(1, LocalDate.parse("2026-12-01"), null, null);
        when(affectationRepository.findById(1)).thenReturn(Optional.of(existante));
        when(affectationRepository.save(existante)).thenReturn(existante);

        var resultat = service.update(1,
                new UpdateAffectationRequest(LocalDate.parse("2026-12-15"), null, null));

        assertThat(resultat.dateFin()).isEqualTo(LocalDate.parse("2026-12-15"));
    }

    // ── deleteCommencee() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : une affectation commencée ne peut pas être supprimée")
    void deleteCommencee() {
        when(affectationRepository.findById(1))
                .thenReturn(Optional.of(affectation(1, LocalDate.parse("2026-11-01"), null, null)));

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
        verify(affectationRepository, never()).delete(any());
    }

    // ── deleteFuture() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F4 : une affectation future peut être supprimée")
    void deleteFuture() {
        Affectation existante = affectation(1, LocalDate.parse("2026-12-01"), null, null);
        when(affectationRepository.findById(1)).thenReturn(Optional.of(existante));

        service.delete(1);

        verify(affectationRepository).delete(existante);
    }

    // ── restitutionOk() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F13 et F14 : la restitution fixe la date de fin et le relevé")
    void restitutionOk() {
        Affectation existante = affectation(1, LocalDate.parse("2026-11-01"), null, null);
        when(affectationRepository.findById(1)).thenReturn(Optional.of(existante));
        when(vueRepository.dernierKm(1)).thenReturn(Optional.of(
                new VueRepository.DernierKm(18000, LocalDate.parse("2026-10-15"))));
        when(affectationRepository.save(existante)).thenReturn(existante);

        var resultat = service.restituer(1,
                new CreateRestitutionRequest(LocalDate.parse("2026-11-24"), 19000));

        assertThat(resultat.dateFin()).isEqualTo(LocalDate.parse("2026-11-24"));
        assertThat(resultat.kmRestitution()).isEqualTo(19000);
    }

    // ── restitutionDejaCloturee() ────────────────────────────────────────────

    @Test
    @DisplayName("F13 : une affectation déjà clôturée est refusée")
    void restitutionDejaCloturee() {
        when(affectationRepository.findById(1)).thenReturn(Optional.of(
                affectation(1, LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-20"), 18500)));

        assertThatThrownBy(() -> service.restituer(1,
                new CreateRestitutionRequest(LocalDate.parse("2026-11-24"), 19000)))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── restitutionDateFuture() ──────────────────────────────────────────────

    @Test
    @DisplayName("F13 : une date future est refusée")
    void restitutionDateFuture() {
        when(affectationRepository.findById(1)).thenReturn(Optional.of(
                affectation(1, LocalDate.parse("2026-11-01"), null, null)));

        assertThatThrownBy(() -> service.restituer(1,
                new CreateRestitutionRequest(LocalDate.parse("2027-01-01"), 19000)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── restitutionKmDecroissant() ───────────────────────────────────────────

    @Test
    @DisplayName("F14 : un kilométrage inférieur au dernier relevé est refusé")
    void restitutionKmDecroissant() {
        when(affectationRepository.findById(1)).thenReturn(Optional.of(
                affectation(1, LocalDate.parse("2026-11-01"), null, null)));
        when(vueRepository.dernierKm(1)).thenReturn(Optional.of(
                new VueRepository.DernierKm(18000, LocalDate.parse("2026-10-15"))));

        assertThatThrownBy(() -> service.restituer(1,
                new CreateRestitutionRequest(LocalDate.parse("2026-11-24"), 17000)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("18000");
    }

    // ── restitutionKmEgal() ──────────────────────────────────────────────────

    @Test
    @DisplayName("D-01 : un kilométrage égal au dernier relevé est accepté")
    void restitutionKmEgal() {
        Affectation existante = affectation(1, LocalDate.parse("2026-11-01"), null, null);
        when(affectationRepository.findById(1)).thenReturn(Optional.of(existante));
        when(vueRepository.dernierKm(1)).thenReturn(Optional.of(
                new VueRepository.DernierKm(18000, LocalDate.parse("2026-10-15"))));
        when(affectationRepository.save(existante)).thenReturn(existante);

        assertThat(service.restituer(1,
                new CreateRestitutionRequest(LocalDate.parse("2026-11-24"), 18000)).kmRestitution())
                .isEqualTo(18000);
    }
}

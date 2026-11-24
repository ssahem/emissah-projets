package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateEntretienRequest;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.AlerteVehicule;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Prestataire;
import com.flottevehicules.flotte_vehicules_api.entity.TypeEntretien;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.EntretienMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.AlerteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.PrestataireRepository;
import com.flottevehicules.flotte_vehicules_api.repository.TypeEntretienRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

/** Tests unitaires de F5, F8, F10, F12 et F14. */
@ExtendWith(MockitoExtension.class)
class EntretienServiceTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.parse("2026-11-24");

    @Mock private EntretienRepository entretienRepository;
    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private TypeEntretienRepository typeEntretienRepository;
    @Mock private PrestataireRepository prestataireRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private AlerteRepository alerteRepository;
    @Mock private VueRepository vueRepository;

    private EntretienService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new EntretienService(entretienRepository, vehiculeRepository,
                typeEntretienRepository, prestataireRepository, affectationRepository,
                alerteRepository, vueRepository,
                new EntretienMapper(typeEntretienRepository, prestataireRepository), clock);
    }

    private Vehicule vehicule(Integer id) {
        Vehicule vehicule = new Vehicule("AB-123-CD", "Peugeot", "308",
                LocalDate.parse("2024-01-15"), 1, "PROPRIETE", false, BigDecimal.TEN);
        ReflectionTestUtils.setField(vehicule, "idVehicule", id);
        return vehicule;
    }

    private Entretien entretien(Integer id, LocalDate date, BigDecimal cout, Integer km) {
        Entretien entretien = new Entretien(date, null, cout, km, 1, 1, 1);
        ReflectionTestUtils.setField(entretien, "idEntretien", id);
        return entretien;
    }

    private void references() {
        lenient().when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1)));
        TypeEntretien type = new TypeEntretien();
        ReflectionTestUtils.setField(type, "idTypeEntretien", 1);
        ReflectionTestUtils.setField(type, "libelle", "Vidange périodique");
        when(typeEntretienRepository.findById(1)).thenReturn(Optional.of(type));
        Prestataire prestataire = new Prestataire();
        ReflectionTestUtils.setField(prestataire, "idPrestataire", 1);
        ReflectionTestUtils.setField(prestataire, "nom", "Garage Central");
        when(prestataireRepository.findById(1)).thenReturn(Optional.of(prestataire));
    }

    private CreateEntretienRequest request(LocalDate date, Integer km) {
        return new CreateEntretienRequest(date, null, 1, 1, 1, BigDecimal.valueOf(320), km);
    }

    @Test
    @DisplayName("F10 : la pagination retourne les métadonnées")
    void findAllPage() {
        when(entretienRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                List.of(entretien(1, AUJOURD_HUI, BigDecimal.TEN, 100)),
                org.springframework.data.domain.PageRequest.of(0, 1), 6));

        var resultat = service.findAll(0, 1, "date,desc");

        assertThat(resultat.content()).hasSize(1);
        assertThat(resultat.totalElements()).isEqualTo(6);
        assertThat(resultat.totalPages()).isEqualTo(6);
    }

    @Test
    @DisplayName("F10 : une taille supérieure à 100 est ramenée à 100")
    void findAllTaillePlafonnee() {
        when(entretienRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.findAll(0, 500, "date,desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(entretienRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("F10 : un tri inconnu est refusé")
    void findAllTriInvalide() {
        assertThatThrownBy(() -> service.findAll(0, 20, "immatriculation,asc"))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    @DisplayName("F10 : un index de page négatif est refusé")
    void findAllPageNegative() {
        assertThatThrownBy(() -> service.findAll(-1, 20, null))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    @DisplayName("F5 : un entretien existant est retourné")
    void findByIdPresent() {
        when(entretienRepository.findById(1))
                .thenReturn(Optional.of(entretien(1, AUJOURD_HUI, BigDecimal.TEN, 100)));

        assertThat(service.findById(1).idEntretien()).isEqualTo(1);
    }

    @Test
    @DisplayName("F5 : un identifiant inconnu renvoie 404")
    void findByIdAbsent() {
        when(entretienRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    @DisplayName("F5 : la création enregistre un entretien valide")
    void createOk() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1)));
        references();
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> {
            Entretien entretien = invocation.getArgument(0);
            ReflectionTestUtils.setField(entretien, "idEntretien", 5);
            return entretien;
        });

        var resultat = service.create(request(AUJOURD_HUI, 19000));

        assertThat(resultat.idEntretien()).isEqualTo(5);
        assertThat(resultat.alerteCreee()).isFalse();
    }

    @Test
    @DisplayName("F12 : un entretien saisi après une affectation crée une alerte")
    void createAvecConflit() {
        references();
        when(affectationRepository.findChevauchements(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of(new Affectation(AUJOURD_HUI, null, 1, 1)));
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultat = service.create(request(AUJOURD_HUI, 19000));

        assertThat(resultat.alerteCreee()).isTrue();
        ArgumentCaptor<AlerteVehicule> captor = ArgumentCaptor.forClass(AlerteVehicule.class);
        verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getMotif()).isEqualTo("CONFLIT_AFFECTATION_ENTRETIEN");
    }

    @Test
    @DisplayName("F12 : une alerte déjà émise n'est pas dupliquée")
    void createConflitAlerteExistante() {
        references();
        when(affectationRepository.findChevauchements(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of(new Affectation(AUJOURD_HUI, null, 1, 1)));
        when(alerteRepository.existsByIdVehiculeAndMotifAndDateEcheance(
                1, "CONFLIT_AFFECTATION_ENTRETIEN", AUJOURD_HUI)).thenReturn(true);
        when(entretienRepository.save(any(Entretien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(request(AUJOURD_HUI, 19000)).alerteCreee()).isFalse();
        verify(alerteRepository, never()).save(any());
    }

    @Test
    @DisplayName("F14 : un kilométrage inférieur au dernier relevé est refusé")
    void createKmDecroissant() {
        references();
        when(vueRepository.dernierKm(1)).thenReturn(Optional.of(
                new VueRepository.DernierKm(18000, LocalDate.parse("2026-10-15"))));

        assertThatThrownBy(() -> service.create(request(AUJOURD_HUI, 17000)))
                .isInstanceOf(DonneeInvalideException.class);
        verify(entretienRepository, never()).save(any());
    }

    @Test
    @DisplayName("F5 : une date avant la mise en service est refusée")
    void createAvantMiseEnService() {
        references();

        assertThatThrownBy(() -> service.create(request(LocalDate.parse("2023-01-01"), 19000)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    @DisplayName("F5 : des dates inversées sont refusées")
    void createDatesInversees() {
        references();

        assertThatThrownBy(() -> service.create(new CreateEntretienRequest(
                AUJOURD_HUI, LocalDate.parse("2026-11-20"), 1, 1, 1, BigDecimal.TEN, 19000)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    @DisplayName("F5 : coût et prestataire sont modifiables")
    void updateOk() {
        references();
        Entretien existant = entretien(1, AUJOURD_HUI, BigDecimal.TEN, 19000);
        when(entretienRepository.findById(1)).thenReturn(Optional.of(existant));
        when(entretienRepository.save(existant)).thenReturn(existant);

        var resultat = service.update(1,
                new UpdateEntretienRequest(BigDecimal.valueOf(350), 1, AUJOURD_HUI, 19000));

        assertThat(resultat.cout()).isEqualByComparingTo("350");
    }

    @Test
    @DisplayName("F5 : la date est figée")
    void updateDateFigee() {
        when(entretienRepository.findById(1))
                .thenReturn(Optional.of(entretien(1, AUJOURD_HUI, BigDecimal.TEN, 19000)));

        assertThatThrownBy(() -> service.update(1, new UpdateEntretienRequest(
                BigDecimal.TEN, 1, LocalDate.parse("2026-11-25"), 19000)))
                .isInstanceOf(ConflitMetierException.class);
    }

    @Test
    @DisplayName("F5 : le kilométrage est figé")
    void updateKmFige() {
        when(entretienRepository.findById(1))
                .thenReturn(Optional.of(entretien(1, AUJOURD_HUI, BigDecimal.TEN, 19000)));

        assertThatThrownBy(() -> service.update(1, new UpdateEntretienRequest(
                BigDecimal.TEN, 1, AUJOURD_HUI, 20000)))
                .isInstanceOf(ConflitMetierException.class);
    }

    @Test
    @DisplayName("F5 : un entretien futur peut être supprimé")
    void deleteFutur() {
        Entretien futur = entretien(1, LocalDate.parse("2026-12-01"), BigDecimal.TEN, 19000);
        when(entretienRepository.findById(1)).thenReturn(Optional.of(futur));

        service.delete(1);

        verify(entretienRepository).delete(futur);
    }

    @Test
    @DisplayName("F5 : un entretien commencé ne peut pas être supprimé")
    void deleteCommence() {
        when(entretienRepository.findById(1))
                .thenReturn(Optional.of(entretien(1, AUJOURD_HUI, BigDecimal.TEN, 19000)));

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
        verify(entretienRepository, never()).delete(any());
    }

    @Test
    @DisplayName("F8 : un véhicule sans entretien renvoie un cumul nul")
    void historiqueVide() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1)));
        when(entretienRepository.findAllByIdVehiculeOrderByDateDebutDesc(1)).thenReturn(List.of());

        var resultat = service.historique(1);

        assertThat(resultat.coutCumule()).isEqualByComparingTo("0");
        assertThat(resultat.entretiens()).isEmpty();
    }

    @Test
    @DisplayName("F8 : l'historique est trié et le cumul vient de la vue")
    void historiqueRenseigne() {
        references();
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1)));
        when(entretienRepository.findAllByIdVehiculeOrderByDateDebutDesc(1)).thenReturn(List.of(
                entretien(2, AUJOURD_HUI, BigDecimal.valueOf(320), 19000),
                entretien(1, LocalDate.parse("2025-06-10"), BigDecimal.valueOf(280), 12000)));
        when(vueRepository.coutEntretiens(1)).thenReturn(BigDecimal.valueOf(600));

        var resultat = service.historique(1);

        assertThat(resultat.coutCumule()).isEqualByComparingTo("600");
        assertThat(resultat.entretiens()).extracting("idEntretien").containsExactly(2, 1);
    }
}

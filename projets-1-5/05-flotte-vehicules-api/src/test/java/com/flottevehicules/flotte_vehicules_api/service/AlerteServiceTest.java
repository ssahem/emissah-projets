package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.entity.AlerteVehicule;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.repository.AlerteRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

/** Tests unitaires de F16 et de la consultation des alertes (A-02). */
@ExtendWith(MockitoExtension.class)
class AlerteServiceTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.parse("2026-11-24");

    @Mock private AlerteRepository alerteRepository;
    @Mock private VueRepository vueRepository;
    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private SiteRepository siteRepository;

    private AlerteService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new AlerteService(alerteRepository, vueRepository, vehiculeRepository,
                siteRepository, clock);
    }

    private Vehicule vehicule(Integer id, String immatriculation, Integer idSite) {
        Vehicule vehicule = new Vehicule(immatriculation, "Peugeot", "308",
                LocalDate.parse("2024-01-15"), idSite, "PROPRIETE", false, BigDecimal.TEN);
        ReflectionTestUtils.setField(vehicule, "idVehicule", id);
        return vehicule;
    }

    private AlerteVehicule alerte(Integer id, Integer idVehicule, Integer idSite) {
        AlerteVehicule alerte = new AlerteVehicule(null, idVehicule, "ECHEANCE_ENTRETIEN",
                "2026-11", AUJOURD_HUI, LocalDate.parse("2026-11-30"),
                8500, 6, "Échéance proche");
        ReflectionTestUtils.setField(alerte, "idAlerte", id);
        return alerte;
    }

    private VueRepository.EcheanceRow echeance(Integer idVehicule, int kmActuel,
                                               LocalDate dateProchaine) {
        return new VueRepository.EcheanceRow(idVehicule, 1, "Vidange périodique",
                LocalDate.parse("2025-11-01"), 10000, 20000,
                dateProchaine, kmActuel, false);
    }

    @Test
    @DisplayName("F16 : une échéance dépassée produit une alerte avec ses écarts")
    void genererAlerteEcheanceDepassee() {
        when(vueRepository.toutesEcheances()).thenReturn(List.of(
                echeance(1, 21000, LocalDate.parse("2026-10-01"))));
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));

        service.genererAlertes();

        ArgumentCaptor<AlerteVehicule> captor = ArgumentCaptor.forClass(AlerteVehicule.class);
        verify(alerteRepository).save(captor.capture());
        assertThat(captor.getValue().getMotif()).isEqualTo("ECHEANCE_ENTRETIEN");
        assertThat(captor.getValue().getEcartKm()).isEqualTo(-1000);
        assertThat(captor.getValue().getEcartJours()).isEqualTo(-54);
        assertThat(captor.getValue().getPeriode()).isEqualTo("2026-11");
    }

    @Test
    @DisplayName("F16 : une échéance à moins de 30 jours produit une alerte")
    void genererAlerteProche() {
        when(vueRepository.toutesEcheances()).thenReturn(List.of(
                echeance(1, 19500, LocalDate.parse("2026-12-10"))));
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));

        service.genererAlertes();

        verify(alerteRepository).save(any(AlerteVehicule.class));
    }

    @Test
    @DisplayName("F16 : une échéance lointaine n'est pas signalée")
    void pasAlerteSiLointaine() {
        when(vueRepository.toutesEcheances()).thenReturn(List.of(
                echeance(1, 12000, LocalDate.parse("2027-11-01"))));

        service.genererAlertes();

        verify(alerteRepository, never()).save(any());
    }

    @Test
    @DisplayName("F16 : une échéance déjà signalée n'est pas signalée deux fois")
    void pasDeDuplication() {
        when(vueRepository.toutesEcheances()).thenReturn(List.of(
                echeance(1, 21000, LocalDate.parse("2026-10-01"))));
        when(alerteRepository.existsByIdVehiculeAndMotifAndDateEcheance(
                1, "ECHEANCE_ENTRETIEN", LocalDate.parse("2026-10-01"))).thenReturn(true);

        service.genererAlertes();

        verify(alerteRepository, never()).save(any());
    }

    @Test
    @DisplayName("F16 : les alertes sont regroupées par site")
    void regroupementParSite() {
        when(vueRepository.toutesEcheances()).thenReturn(List.of());
        when(alerteRepository.findAllByOrderByDateAlerteDescIdAlerteDesc()).thenReturn(List.of(
                alerte(1, 1, 1), alerte(2, 2, 2)));
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(vehiculeRepository.findById(2)).thenReturn(Optional.of(vehicule(2, "CD-456-EF", 2)));
        when(siteRepository.findById(1)).thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));
        when(siteRepository.findById(2)).thenReturn(Optional.of(new Site("Lyon", "Lyon", "adresse")));

        var resultat = service.genererAlertes();

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).nomSite()).isEqualTo("Paris Centre");
        assertThat(resultat.get(1).nomSite()).isEqualTo("Lyon");
    }

    @Test
    @DisplayName("Les alertes peuvent être filtrées par véhicule")
    void listerFiltre() {
        when(alerteRepository.findAllByOrderByDateAlerteDescIdAlerteDesc()).thenReturn(List.of(
                alerte(1, 1, 1), alerte(2, 2, 1)));
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(siteRepository.findById(1)).thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));

        assertThat(service.lister(1)).hasSize(1);
    }

    @Test
    @DisplayName("Une alerte inconnue renvoie 404")
    void findByIdAbsent() {
        when(alerteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}

package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateVehiculeRequest;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.TypeEntretien;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.VehiculeMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
import com.flottevehicules.flotte_vehicules_api.repository.SiteRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de F2, F9, F15, F17 et F18.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de F2, F9, F15, F17 et F18.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDate AUJOURD_HUI = LocalDate.parse("2026-11-24");

    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private SiteRepository siteRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private EntretienRepository entretienRepository;
    @Mock private TypeEntretienRepository typeEntretienRepository;
    @Mock private VueRepository vueRepository;

    private VehiculeService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new VehiculeService(vehiculeRepository, siteRepository, affectationRepository,
                entretienRepository, typeEntretienRepository, vueRepository,
                new VehiculeMapper(siteRepository, vueRepository), clock);
    }

    // ── vehicule() ───────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour vehicule().
     */

    private Vehicule vehicule(Integer id, String immatriculation, Integer idSite) {
        Vehicule vehicule = new Vehicule(immatriculation, "Peugeot", "308",
                LocalDate.parse("2024-01-15"), idSite, "PROPRIETE", false, BigDecimal.TEN);
        ReflectionTestUtils.setField(vehicule, "idVehicule", id);
        return vehicule;
    }

    // ── request() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour request().
     */

    private CreateVehiculeRequest request(String immatriculation, LocalDate date, Integer idSite,
                                          String mode, BigDecimal prix) {
        return new CreateVehiculeRequest(immatriculation, "Peugeot", "308", date, idSite,
                mode, false, prix);
    }

    // ── findAllTriee() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : la liste est triée par immatriculation")
    void findAllTriee() {
        when(vehiculeRepository.findAllByOrderByImmatriculationAsc())
                .thenReturn(List.of(vehicule(1, "AB-123-CD", 1), vehicule(2, "CD-456-EF", 1)));

        assertThat(service.findAll(null, null)).extracting("immatriculation")
                .containsExactly("AB-123-CD", "CD-456-EF");
    }

    // ── findAllRecherche() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : la recherche partielle est déléguée au repository")
    void findAllRecherche() {
        when(vehiculeRepository.search("AB-123", "Peugeot"))
                .thenReturn(List.of(vehicule(1, "AB-123-CD", 1)));

        assertThat(service.findAll("AB-123", "Peugeot")).hasSize(1);
        verify(vehiculeRepository, never()).findAllByOrderByImmatriculationAsc();
    }

    // ── findByIdPresent() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un véhicule existant est retourné")
    void findByIdPresent() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));

        assertThat(service.findById(1).immatriculation()).isEqualTo("AB-123-CD");
    }

    // ── findByIdAbsent() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un identifiant inconnu renvoie 404")
    void findByIdAbsent() {
        when(vehiculeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── createOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : la création accepte un véhicule en propriété")
    void createOk() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.empty());
        when(siteRepository.findById(1)).thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultat = service.create(request("AB-123-CD", LocalDate.parse("2024-01-15"), 1,
                "PROPRIETE", BigDecimal.valueOf(25000)));

        assertThat(resultat.immatriculation()).isEqualTo("AB-123-CD");
        assertThat(resultat.modeAcquisition()).isEqualTo("PROPRIETE");
    }

    // ── createDoublon() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : une immatriculation déjà utilisée est refusée")
    void createDoublon() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD"))
                .thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));

        assertThatThrownBy(() -> service.create(request("AB-123-CD",
                LocalDate.parse("2024-01-15"), 1, "PROPRIETE", BigDecimal.TEN)))
                .isInstanceOf(ConflitMetierException.class);
    }

    // ── createDateFuture() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : une mise en service future est refusée")
    void createDateFuture() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("AB-123-CD",
                LocalDate.parse("2027-01-01"), 1, "PROPRIETE", BigDecimal.TEN)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── createModeInvalide() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un mode d'acquisition inconnu est refusé")
    void createModeInvalide() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.empty());
        when(siteRepository.findById(1)).thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));

        assertThatThrownBy(() -> service.create(request("AB-123-CD",
                LocalDate.parse("2024-01-15"), 1, "ACHAT", BigDecimal.TEN)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── createSiteInconnu() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un site inconnu est refusé")
    void createSiteInconnu() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.empty());
        when(siteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("AB-123-CD",
                LocalDate.parse("2024-01-15"), 99, "PROPRIETE", BigDecimal.TEN)))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── createPrixAchatInterdit() ────────────────────────────────────────────

    @Test
    @DisplayName("F2 : un prix d'achat est refusé en location")
    void createPrixAchatInterdit() {
        when(vehiculeRepository.findByImmatriculation("AB-123-CD")).thenReturn(Optional.empty());
        when(siteRepository.findById(1)).thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));

        assertThatThrownBy(() -> service.create(request("AB-123-CD",
                LocalDate.parse("2024-01-15"), 1, "LEASING", BigDecimal.TEN)))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── updateTransfert() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : le transfert de site est autorisé")
    void updateTransfert() {
        Vehicule vehicule = vehicule(1, "AB-123-CD", 1);
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule));
        when(siteRepository.findById(2)).thenReturn(Optional.of(new Site("Lyon", "Lyon", "adresse")));
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);

        var resultat = service.update(1, new UpdateVehiculeRequest(2, "Renault", "Clio"));

        assertThat(resultat.idSite()).isEqualTo(2);
        assertThat(resultat.immatriculation()).isEqualTo("AB-123-CD");
    }

    // ── deleteAvecAffectation() ──────────────────────────────────────────────

    @Test
    @DisplayName("F2 : suppression refusée en cas d'affectation")
    void deleteAvecAffectation() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(affectationRepository.existsByIdVehicule(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
        verify(vehiculeRepository, never()).delete(any());
    }

    // ── deleteAvecEntretien() ────────────────────────────────────────────────

    @Test
    @DisplayName("F2 : suppression refusée en cas d'entretien")
    void deleteAvecEntretien() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(entretienRepository.existsByIdVehicule(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
    }

    // ── prochainEntretien() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : le seuil déclenchant est calculé depuis la vue d'échéance")
    void prochainEntretien() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(vueRepository.echeancesVehicule(1)).thenReturn(List.of(new VueRepository.EcheanceRow(
                1, 1, "Vidange périodique", LocalDate.parse("2025-10-15"), 12000, 27000,
                LocalDate.parse("2026-10-15"), 19500, true)));

        var resultat = service.prochainEntretien(1);

        assertThat(resultat.kmCible()).isEqualTo(27000);
        assertThat(resultat.seuilDeclencheur()).isEqualTo("DUREE");
        assertThat(resultat.enRetard()).isTrue();
    }

    // ── coutEntretiens() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F18 : le coût total est celui des entretiens, réparti par type")
    void coutEntretiens() {
        when(vehiculeRepository.findById(1)).thenReturn(Optional.of(vehicule(1, "AB-123-CD", 1)));
        when(vueRepository.bilanVehicule(1)).thenReturn(Optional.of(new VueRepository.BilanRow(
                1, "AB-123-CD", BigDecimal.valueOf(600), 2, 19000, new BigDecimal("0.03"))));
        when(vueRepository.coutsVehicule(1)).thenReturn(Optional.of(new VueRepository.CoutRow(
                1, "AB-123-CD", BigDecimal.valueOf(25000), BigDecimal.ZERO,
                BigDecimal.valueOf(600), BigDecimal.valueOf(89), BigDecimal.valueOf(25689))));
        TypeEntretien type = new TypeEntretien();
        ReflectionTestUtils.setField(type, "idTypeEntretien", 1);
        ReflectionTestUtils.setField(type, "libelle", "Vidange périodique");
        when(typeEntretienRepository.findAll()).thenReturn(List.of(type));
        when(entretienRepository.findAllByIdVehiculeOrderByDateDebutDesc(1)).thenReturn(List.of(
                new Entretien(LocalDate.parse("2026-10-15"), null, BigDecimal.valueOf(320), 19000, 1, 1, 1),
                new Entretien(LocalDate.parse("2025-06-10"), null, BigDecimal.valueOf(280), 12000, 1, 1, 1)));

        var resultat = service.cout(1);

        assertThat(resultat.coutTotal()).isEqualByComparingTo("600");
        assertThat(resultat.repartition()).hasSize(1);
        assertThat(resultat.repartition().get(0).cout()).isEqualByComparingTo("600");
    }

    // ── disponiblesPeriodeInvalide() ─────────────────────────────────────────

    @Test
    @DisplayName("F17 : une période inversée est refusée")
    void disponiblesPeriodeInvalide() {
        assertThatThrownBy(() -> service.disponibles(
                LocalDate.parse("2026-11-30"), LocalDate.parse("2026-11-01")))
                .isInstanceOf(DonneeInvalideException.class);
    }

    // ── disponiblesGroupesParSite() ──────────────────────────────────────────

    @Test
    @DisplayName("F17 : les véhicules occupés sont exclus et les libres groupés par site")
    void disponiblesGroupesParSite() {
        when(vehiculeRepository.findAllByOrderByImmatriculationAsc()).thenReturn(List.of(
                vehicule(1, "AB-123-CD", 1), vehicule(2, "CD-456-EF", 2)));
        LocalDate debut = LocalDate.parse("2026-11-23");
        LocalDate fin = LocalDate.parse("2026-11-29");
        when(affectationRepository.findChevauchements(1, debut, fin)).thenReturn(List.of());
        when(affectationRepository.findChevauchements(2, debut, fin))
                .thenReturn(List.of(new Affectation(debut, fin, 2, 1)));
        when(vueRepository.echeancesVehicule(1)).thenReturn(List.of(new VueRepository.EcheanceRow(
                1, 1, "Vidange", LocalDate.parse("2025-11-01"), 10000, 25000,
                LocalDate.parse("2027-11-01"), 19000, false)));
        when(siteRepository.findById(1))
                .thenReturn(Optional.of(new Site("Paris Centre", "Paris", "adresse")));

        var resultat = service.disponibles(debut, fin);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).idSite()).isEqualTo(1);
        assertThat(resultat.get(0).vehicules()).extracting("idVehicule").containsExactly(1);
    }
}

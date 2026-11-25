package com.flottevehicules.flotte_vehicules_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flottevehicules.flotte_vehicules_api.dto.request.CreateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.dto.request.UpdateSiteRequest;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Site;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.exception.ConflitMetierException;
import com.flottevehicules.flotte_vehicules_api.exception.DonneeInvalideException;
import com.flottevehicules.flotte_vehicules_api.exception.RessourceIntrouvableException;
import com.flottevehicules.flotte_vehicules_api.mapper.SiteMapper;
import com.flottevehicules.flotte_vehicules_api.repository.AffectationRepository;
import com.flottevehicules.flotte_vehicules_api.repository.EntretienRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de F1, F6 et F19.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires de F1, F6 et F19.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDate AUJOURD_HUI = LocalDate.parse("2026-11-24");

    @Mock private SiteRepository siteRepository;
    @Mock private VehiculeRepository vehiculeRepository;
    @Mock private AffectationRepository affectationRepository;
    @Mock private EntretienRepository entretienRepository;
    @Mock private VueRepository vueRepository;

    private SiteService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
        service = new SiteService(siteRepository, vehiculeRepository, affectationRepository,
                entretienRepository, vueRepository, new SiteMapper(), clock);
    }

    // ── site() ───────────────────────────────────────────────────────────────

    /**
     * Prépare un site de test avec des valeurs nominales.
     */

    private Site site(Integer id, String nom, String ville) {
        Site site = new Site(nom, ville, "1 rue de Paris");
        ReflectionTestUtils.setField(site, "idSite", id);
        return site;
    }

    // ── vehicule() ───────────────────────────────────────────────────────────

    /**
     * Prépare un site de test avec des valeurs nominales.
     */

    private Vehicule vehicule(Integer id) {
        Vehicule vehicule = new Vehicule("AB-123-CD", "Peugeot", "308",
                LocalDate.parse("2024-01-15"), 1, "PROPRIETE", false, BigDecimal.TEN);
        ReflectionTestUtils.setField(vehicule, "idVehicule", id);
        return vehicule;
    }

    // ── findAllTriee() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la liste est triée par ville puis par nom")
    void findAllTriee() {
        when(siteRepository.findAllByOrderByVilleAscNomAsc())
                .thenReturn(List.of(site(1, "Paris Centre", "Paris"), site(2, "Lyon Part-Dieu", "Lyon")));

        var resultat = service.findAll();

        assertThat(resultat).extracting("ville").containsExactly("Paris", "Lyon");
    }

    // ── findByIdPresent() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : un site existant est retourné")
    void findByIdPresent() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));

        assertThat(service.findById(1).nom()).isEqualTo("Paris Centre");
    }

    // ── findByIdAbsent() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : un identifiant inconnu renvoie 404")
    void findByIdAbsent() {
        when(siteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── createOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la création enregistre un site avec sa ville")
    void createOk() {
        when(siteRepository.findByNom("Nantes")).thenReturn(Optional.empty());
        when(siteRepository.save(any(Site.class))).thenAnswer(invocation -> {
            Site site = invocation.getArgument(0);
            ReflectionTestUtils.setField(site, "idSite", 4);
            return site;
        });

        var resultat = service.create(new CreateSiteRequest("Nantes", "Nantes", "2 rue de la Loire"));

        assertThat(resultat.idSite()).isEqualTo(4);
        assertThat(resultat.ville()).isEqualTo("Nantes");
    }

    // ── createDoublon() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : un nom déjà utilisé est refusé en 409")
    void createDoublon() {
        when(siteRepository.findByNom("Paris Centre"))
                .thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));

        assertThatThrownBy(() -> service.create(new CreateSiteRequest("Paris Centre", "Paris", "adresse")))
                .isInstanceOf(ConflitMetierException.class);
        verify(siteRepository, never()).save(any());
    }

    // ── updateOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la modification touche adresse et ville")
    void updateOk() {
        Site site = site(1, "Paris Centre", "Paris");
        when(siteRepository.findById(1)).thenReturn(Optional.of(site));
        when(siteRepository.save(site)).thenReturn(site);

        var resultat = service.update(1, new UpdateSiteRequest("14 rue de la Paix", "Lyon"));

        assertThat(resultat.ville()).isEqualTo("Lyon");
        assertThat(resultat.adresse()).isEqualTo("14 rue de la Paix");
        assertThat(resultat.nom()).isEqualTo("Paris Centre");
    }

    // ── deleteOk() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la suppression est autorisée sans véhicule")
    void deleteOk() {
        Site site = site(1, "Paris Centre", "Paris");
        when(siteRepository.findById(1)).thenReturn(Optional.of(site));

        service.delete(1);

        verify(siteRepository).delete(site);
    }

    // ── deleteAvecVehicules() ────────────────────────────────────────────────

    @Test
    @DisplayName("F1 : la suppression est refusée si le site a des véhicules")
    void deleteAvecVehicules() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));
        when(vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(1))
                .thenReturn(List.of(vehicule(1)));

        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(ConflitMetierException.class);
        verify(siteRepository, never()).delete(any());
    }

    // ── vehiculesLibre() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("F6 : un véhicule libre est signalé LIBRE")
    void vehiculesLibre() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));
        when(vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(1))
                .thenReturn(List.of(vehicule(1)));
        when(entretienRepository.findImmobilisationsChevauchant(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of());
        when(affectationRepository.findChevauchements(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of());

        assertThat(service.vehicules(1).get(0).etatAffectation()).isEqualTo("LIBRE");
    }

    // ── vehiculesAffecte() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F6 : un véhicule affecté est signalé AFFECTE")
    void vehiculesAffecte() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));
        when(vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(1))
                .thenReturn(List.of(vehicule(1)));
        when(entretienRepository.findImmobilisationsChevauchant(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of());
        when(affectationRepository.findChevauchements(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of(new Affectation(AUJOURD_HUI, null, 1, 1)));

        assertThat(service.vehicules(1).get(0).etatAffectation()).isEqualTo("AFFECTE");
    }

    // ── vehiculesImmobilise() ────────────────────────────────────────────────

    @Test
    @DisplayName("F6 : l'immobilisation prend le pas sur l'affectation")
    void vehiculesImmobilise() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));
        when(vehiculeRepository.findAllByIdSiteOrderByImmatriculationAsc(1))
                .thenReturn(List.of(vehicule(1)));
        Entretien entretien = new Entretien(AUJOURD_HUI, null, BigDecimal.TEN, 100, 1, 1, 1);
        when(entretienRepository.findImmobilisationsChevauchant(1, AUJOURD_HUI, AUJOURD_HUI))
                .thenReturn(List.of(entretien));

        assertThat(service.vehicules(1).get(0).etatAffectation()).isEqualTo("IMMOBILISE");
        verify(affectationRepository, never()).findChevauchements(1, AUJOURD_HUI, AUJOURD_HUI);
    }

    // ── kilometrageTotal() ───────────────────────────────────────────────────

    @Test
    @DisplayName("F19 : le kilométrage total est la somme des relevés du mois")
    void kilometrageTotal() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));
        when(vueRepository.kilometragesParSite(1, LocalDate.parse("2026-11-01"),
                LocalDate.parse("2026-11-30"))).thenReturn(List.of(
                new VueRepository.KilometrageRow(1, "AB-123-CD", 18500),
                new VueRepository.KilometrageRow(2, "CD-456-EF", 8500)));

        var resultat = service.kilometrage(1, "2026-11");

        assertThat(resultat.kmTotal()).isEqualTo(27000);
        assertThat(resultat.vehicules()).hasSize(2);
        assertThat(resultat.mois()).isEqualTo("2026-11");
    }

    // ── kilometrageMoisInvalide() ────────────────────────────────────────────

    @Test
    @DisplayName("F19 : un mois invalide est refusé en 400")
    void kilometrageMoisInvalide() {
        when(siteRepository.findById(1)).thenReturn(Optional.of(site(1, "Paris Centre", "Paris")));

        assertThatThrownBy(() -> service.kilometrage(1, "novembre"))
                .isInstanceOf(DonneeInvalideException.class);
    }
}

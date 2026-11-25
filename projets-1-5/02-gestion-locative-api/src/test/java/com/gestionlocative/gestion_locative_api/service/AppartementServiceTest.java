package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.dto.request.CreateAppartementRequest;
import com.gestionlocative.gestion_locative_api.dto.response.HistoriqueBailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleAppartementsLibresResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PeriodeOccupationResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.BailLocataire;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.exception.AppartementEtageHorsImmeubleException;
import com.gestionlocative.gestion_locative_api.exception.AppartementNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.AppartementNumeroDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.SurfaceRechercheInvalideException;
import com.gestionlocative.gestion_locative_api.mapper.AppartementMapper;
import com.gestionlocative.gestion_locative_api.mapper.BailMapper;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailLocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.RevisionLoyerRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires du service des appartements (F3).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des appartements (F3).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mapper réel.
 * CAS COUVERTS : étage hors immeuble, numéro en doublon, suppression louée.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class AppartementServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private AppartementRepository appartementRepository;
    @Mock private ImmeubleRepository immeubleRepository;
    @Mock private BailRepository bailRepository;
    @Mock private BailLocataireRepository bailLocataireRepository;
    @Mock private LocataireRepository locataireRepository;
    @Mock private RevisionLoyerRepository revisionLoyerRepository;

    private AppartementService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        Clock horlogeFixe = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        service = new AppartementService(appartementRepository, immeubleRepository, bailRepository,
                bailLocataireRepository, locataireRepository, revisionLoyerRepository,
                new AppartementMapper(), new BailMapper(), new ImmeubleMapper(), horlogeFixe);
    }

    // ── request() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour request().
     */

    private CreateAppartementRequest request(int etage) {
        return new CreateAppartementRequest(
                "1A", new BigDecimal("45.50"), 2, etage, 1);
    }

    // ── create_throwsWhenEtageHorsImmeuble() ─────────────────────────────────

    @Test
    @DisplayName("La création refuse un étage supérieur au nombre d'étages")
    void create_throwsWhenEtageHorsImmeuble() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 2)));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(request(3)))
                .isInstanceOf(AppartementEtageHorsImmeubleException.class);
    }

    // ── create_throwsWhenNumeroDejaUtilise() ─────────────────────────────────

    @Test
    @DisplayName("La création refuse un numéro déjà utilisé dans l'immeuble")
    void create_throwsWhenNumeroDejaUtilise() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3)));
        when(appartementRepository.findByNumeroAndIdImmeuble("1A", 1))
                .thenReturn(Optional.of(new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1)));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(request(1)))
                .isInstanceOf(AppartementNumeroDejaUtiliseException.class);
    }

    // ── delete_throwsWhenBailExists() ────────────────────────────────────────

    @Test
    @DisplayName("La suppression refuse un appartement déjà loué")
    void delete_throwsWhenBailExists() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(appartementRepository.findById(1)).thenReturn(Optional.of(
                new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1)));
        when(bailRepository.countByIdAppartement(1)).thenReturn(1L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(AppartementNonSupprimableException.class);
    }

    // ── search_throwsWhenBornesInversees() ───────────────────────────────────

    @Test
    @DisplayName("F9 — une surface minimale supérieure à la maximale est refusée")
    void search_throwsWhenBornesInversees() {
        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.search(
                new BigDecimal("90"), new BigDecimal("50"), null))
                .isInstanceOf(SurfaceRechercheInvalideException.class);
    }

    // ── findBaux_renvoieHistoriqueEnCours() ──────────────────────────────────

    @Test
    @DisplayName("F8 — l'historique signale le bail en cours avec son locataire")
    void findBaux_renvoieHistoriqueEnCours() {
        // ── Préparation ──────────────────────────────────────────────────────
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1);
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        Bail bail = new Bail(LocalDate.of(2026, 6, 1), null, BigDecimal.ZERO,
                BigDecimal.ZERO, 2, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(bail, "idBail", 3);
        Locataire locataire = new Locataire("Traore", "Kofi", "kofi@example.test", "0102");
        ReflectionTestUtils.setField(locataire, "idLocataire", 4);
        when(appartementRepository.findById(2)).thenReturn(Optional.of(appartement));
        when(bailRepository.findAllByIdAppartementOrderByDateDebutDesc(2))
                .thenReturn(List.of(bail));
        when(bailLocataireRepository.findByIdIdBail(3))
                .thenReturn(List.of(new BailLocataire(3, 4)));
        when(locataireRepository.findById(4)).thenReturn(Optional.of(locataire));
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(3))
                .thenReturn(List.of(new RevisionLoyer(3, LocalDate.of(2026, 6, 1),
                        new BigDecimal("850.00"))));

        // ── Action ───────────────────────────────────────────────────────────
        List<HistoriqueBailResponse> resultat = service.findBaux(2);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(1);
        assertThat(resultat.getFirst().enCours()).isTrue();
        assertThat(resultat.getFirst().nomLocataire()).isEqualTo("Traore");
        assertThat(resultat.getFirst().loyersPasses()).isEmpty();
    }

    // ── findLibres_groupeParImmeubleEtPorteDernierLoyer() ────────────────────

    @Test
    @DisplayName("F17 — les appartements libres sont groupés par immeuble avec leur dernier loyer")
    void findLibres_groupeParImmeubleEtPorteDernierLoyer() {
        // ── Préparation ──────────────────────────────────────────────────────
        Immeuble immeuble = new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3);
        ReflectionTestUtils.setField(immeuble, "idImmeuble", 1);
        Appartement loue = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1);
        ReflectionTestUtils.setField(loue, "idAppartement", 2);
        Appartement libre = new Appartement("2A", new BigDecimal("60.00"), 3, 2, 1);
        ReflectionTestUtils.setField(libre, "idAppartement", 3);
        Bail bail = new Bail(LocalDate.of(2026, 6, 1), LocalDate.of(2027, 5, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 2, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(bail, "idBail", 5);
        when(bailRepository.findIdAppartementsOccupesLe(LocalDate.of(2026, 12, 1)))
                .thenReturn(List.of(2));
        when(appartementRepository.findAllByOrderByEtageAscSurfaceAsc())
                .thenReturn(List.of(loue, libre));
        when(bailRepository.findAllByIdAppartementOrderByDateDebutDesc(3)).thenReturn(List.of(bail));
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(5))
                .thenReturn(List.of(new RevisionLoyer(5, LocalDate.of(2026, 6, 1),
                        new BigDecimal("850.00"))));
        when(immeubleRepository.findAllById(Set.of(1))).thenReturn(List.of(immeuble));

        // ── Action ───────────────────────────────────────────────────────────
        List<ImmeubleAppartementsLibresResponse> resultat =
                service.findLibres(LocalDate.of(2026, 12, 1));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(1);
        assertThat(resultat.getFirst().idImmeuble()).isEqualTo(1);
        assertThat(resultat.getFirst().appartements()).hasSize(1);
        assertThat(resultat.getFirst().appartements().getFirst().numero()).isEqualTo("2A");
        assertThat(resultat.getFirst().appartements().getFirst().dernierLoyer())
                .isEqualByComparingTo("850.00");
    }

    // ── findOccupation_decoupePaliersEtVacances() ────────────────────────────

    @Test
    @DisplayName("F18 — l'occupation alterne baux, paliers de loyer et vacances")
    void findOccupation_decoupePaliersEtVacances() {
        // ── Préparation ──────────────────────────────────────────────────────
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1,
                LocalDate.of(2026, 1, 1));
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        Bail premier = new Bail(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 2, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(premier, "idBail", 3);
        Bail second = new Bail(LocalDate.of(2026, 5, 1), null,
                BigDecimal.ZERO, BigDecimal.ZERO, 2, new BigDecimal("900.00"));
        ReflectionTestUtils.setField(second, "idBail", 4);
        Locataire locataire = new Locataire("Traore", "Kofi", "kofi@example.test", "0102");
        ReflectionTestUtils.setField(locataire, "idLocataire", 7);
        when(appartementRepository.findById(2)).thenReturn(Optional.of(appartement));
        when(bailRepository.findAllByIdAppartementOrderByDateDebutAsc(2))
                .thenReturn(List.of(premier, second));
        when(bailLocataireRepository.findByIdIdBail(3))
                .thenReturn(List.of(new BailLocataire(3, 7)));
        when(bailLocataireRepository.findByIdIdBail(4))
                .thenReturn(List.of(new BailLocataire(4, 7)));
        when(locataireRepository.findById(7)).thenReturn(Optional.of(locataire));
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetAsc(3)).thenReturn(List.of(
                new RevisionLoyer(3, LocalDate.of(2026, 1, 1), new BigDecimal("800.00")),
                new RevisionLoyer(3, LocalDate.of(2026, 2, 1), new BigDecimal("850.00"))));
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetAsc(4)).thenReturn(List.of(
                new RevisionLoyer(4, LocalDate.of(2026, 5, 1), new BigDecimal("900.00"))));

        // ── Action ───────────────────────────────────────────────────────────
        List<PeriodeOccupationResponse> resultat = service.findOccupation(2);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(0).type()).isEqualTo("LOUE");
        assertThat(resultat.get(0).fin()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(resultat.get(0).loyerMensuel()).isEqualByComparingTo("800.00");
        assertThat(resultat.get(1).loyerMensuel()).isEqualByComparingTo("850.00");
        assertThat(resultat.get(2).type()).isEqualTo("VACANT");
        assertThat(resultat.get(3).debut()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(resultat.get(3).fin()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(resultat.get(3).nomLocataire()).isEqualTo("Traore");
    }

    // ── findOccupation_sansBailRenvoieUneVacance() ───────────────────────────

    @Test
    @DisplayName("F18 — un appartement jamais loué renvoie une seule vacance ouverte")
    void findOccupation_sansBailRenvoieUneVacance() {
        // ── Préparation ──────────────────────────────────────────────────────
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1,
                LocalDate.of(2026, 3, 1));
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        when(appartementRepository.findById(2)).thenReturn(Optional.of(appartement));
        when(bailRepository.findAllByIdAppartementOrderByDateDebutAsc(2)).thenReturn(List.of());

        // ── Action ───────────────────────────────────────────────────────────
        List<PeriodeOccupationResponse> resultat = service.findOccupation(2);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(1);
        assertThat(resultat.getFirst().type()).isEqualTo("VACANT");
        assertThat(resultat.getFirst().debut()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(resultat.getFirst().fin()).isNull();
    }
}

package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.dto.request.UpdateImmeubleRequest;
import com.gestionlocative.gestion_locative_api.dto.response.AppartementOccupationResponse;
import com.gestionlocative.gestion_locative_api.dto.response.RevenuImmeubleResponse;
import com.gestionlocative.gestion_locative_api.entity.Appartement;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.exception.ImmeubleNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.MoisInvalideException;
import com.gestionlocative.gestion_locative_api.exception.NbEtagesInsuffisantException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNotFoundException;
import com.gestionlocative.gestion_locative_api.mapper.AppartementMapper;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.ProprietaireRepository;
import com.gestionlocative.gestion_locative_api.repository.RevisionLoyerRepository;
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
 * Tests unitaires du service des immeubles (F2).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des immeubles (F2).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mapper réel.
 * CAS COUVERTS : propriétaire inconnu, étage maximal, suppression occupée.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ImmeubleServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ImmeubleRepository immeubleRepository;
    @Mock private ProprietaireRepository proprietaireRepository;
    @Mock private AppartementRepository appartementRepository;
    @Mock private BailRepository bailRepository;
    @Mock private RevisionLoyerRepository revisionLoyerRepository;

    private ImmeubleService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        Clock horlogeFixe = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        service = new ImmeubleService(immeubleRepository, proprietaireRepository,
                appartementRepository, bailRepository, revisionLoyerRepository,
                new ImmeubleMapper(), new AppartementMapper(), horlogeFixe);
    }

    // ── update_throwsWhenProprietaireMissing() ───────────────────────────────

    @Test
    @DisplayName("La modification refuse un propriétaire inconnu")
    void update_throwsWhenProprietaireMissing() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3)));
        when(proprietaireRepository.existsById(99)).thenReturn(false);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(1,
                new UpdateImmeubleRequest("12 rue des Acacias", "75001", "Paris", 99, 3)))
                .isInstanceOf(ProprietaireNotFoundException.class);
    }

    // ── update_throwsWhenNbEtagesTooLow() ────────────────────────────────────

    @Test
    @DisplayName("La modification refuse un nombre d'étages inférieur à l'étage occupé")
    void update_throwsWhenNbEtagesTooLow() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3)));
        when(proprietaireRepository.existsById(1)).thenReturn(true);
        when(appartementRepository.findMaxEtageByIdImmeuble(1)).thenReturn(2);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(1,
                new UpdateImmeubleRequest("12 rue des Acacias", "75001", "Paris", 1, 1)))
                .isInstanceOf(NbEtagesInsuffisantException.class);
    }

    // ── delete_throwsWhenAppartementsExist() ─────────────────────────────────

    @Test
    @DisplayName("La suppression refuse un immeuble qui possède des appartements")
    void delete_throwsWhenAppartementsExist() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3)));
        when(appartementRepository.countByIdImmeuble(1)).thenReturn(1L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ImmeubleNonSupprimableException.class);
    }

    // ── findAppartements_renvoieEtatOccupation() ─────────────────────────────

    @Test
    @DisplayName("F6 — un bail en cours marque l'appartement comme loué")
    void findAppartements_renvoieEtatOccupation() {
        // ── Préparation ──────────────────────────────────────────────────────
        Immeuble immeuble = new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3);
        ReflectionTestUtils.setField(immeuble, "idImmeuble", 1);
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1);
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        Bail bail = new Bail(LocalDate.of(2026, 6, 1), null, BigDecimal.ZERO,
                BigDecimal.ZERO, 2, new BigDecimal("850.00"));
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(immeuble));
        when(appartementRepository.findAllByIdImmeubleOrderByEtageAscSurfaceAsc(1))
                .thenReturn(List.of(appartement));
        when(bailRepository.findAllByIdAppartementOrderByDateDebutDesc(2))
                .thenReturn(List.of(bail));

        // ── Action ───────────────────────────────────────────────────────────
        List<AppartementOccupationResponse> resultat = service.findAppartements(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(1);
        assertThat(resultat.getFirst().occupe()).isTrue();
        assertThat(resultat.getFirst().statutOccupation()).isEqualTo("LOUE");
    }

    // ── calculerRevenu_prorataEtRevisions() ──────────────────────────────────

    @Test
    @DisplayName("F19 — le revenu applique le prorata journalier et les révisions de loyer")
    void calculerRevenu_prorataEtRevisions() {
        // ── Préparation : bail du 1er au 15 novembre, révisé le 10 ───────────
        Immeuble immeuble = new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3);
        ReflectionTestUtils.setField(immeuble, "idImmeuble", 1);
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1);
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        Bail bail = new Bail(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 15),
                BigDecimal.ZERO, BigDecimal.ZERO, 2, new BigDecimal("900.00"));
        ReflectionTestUtils.setField(bail, "idBail", 3);
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(immeuble));
        when(appartementRepository.findAllByIdImmeubleOrderByEtageAscSurfaceAsc(1))
                .thenReturn(List.of(appartement));
        when(bailRepository.findActifsByIdImmeubleEntre(
                1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(bail));
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetAsc(3)).thenReturn(List.of(
                new RevisionLoyer(3, LocalDate.of(2026, 1, 1), new BigDecimal("800.00")),
                new RevisionLoyer(3, LocalDate.of(2026, 11, 10), new BigDecimal("1000.00"))));

        // ── Action ───────────────────────────────────────────────────────────
        RevenuImmeubleResponse resultat = service.calculerRevenu(1, "2026-11");

        // ── Vérification : 9 jours à 800 + 6 jours à 1000 = 13200 / 30 = 440 ─
        assertThat(resultat.detail()).hasSize(1);
        assertThat(resultat.detail().getFirst().joursOccupes()).isEqualTo(15);
        assertThat(resultat.detail().getFirst().revenu()).isEqualByComparingTo("440.00");
        assertThat(resultat.detail().getFirst().tauxOccupation()).isEqualByComparingTo("50.00");
        assertThat(resultat.revenuTotal()).isEqualByComparingTo("440.00");
        assertThat(resultat.tauxOccupation()).isEqualByComparingTo("50.00");
    }

    // ── calculerRevenu_sansBailRenvoieZeros() ────────────────────────────────

    @Test
    @DisplayName("F19 — un immeuble sans bail renvoie un revenu et une occupation nuls")
    void calculerRevenu_sansBailRenvoieZeros() {
        // ── Préparation ──────────────────────────────────────────────────────
        Immeuble immeuble = new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3);
        ReflectionTestUtils.setField(immeuble, "idImmeuble", 1);
        Appartement appartement = new Appartement("1A", new BigDecimal("45.50"), 2, 1, 1);
        ReflectionTestUtils.setField(appartement, "idAppartement", 2);
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(immeuble));
        when(appartementRepository.findAllByIdImmeubleOrderByEtageAscSurfaceAsc(1))
                .thenReturn(List.of(appartement));
        when(bailRepository.findActifsByIdImmeubleEntre(
                1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of());

        // ── Action ───────────────────────────────────────────────────────────
        RevenuImmeubleResponse resultat = service.calculerRevenu(1, "2026-11");

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.revenuTotal()).isEqualByComparingTo("0.00");
        assertThat(resultat.tauxOccupation()).isEqualByComparingTo("0.00");
        assertThat(resultat.detail()).hasSize(1);
        assertThat(resultat.detail().getFirst().revenu()).isEqualByComparingTo("0.00");
    }

    // ── calculerRevenu_refuseMoisInvalide() ──────────────────────────────────

    @Test
    @DisplayName("F19 — un mois mal formé est refusé")
    void calculerRevenu_refuseMoisInvalide() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(immeubleRepository.findById(1)).thenReturn(Optional.of(
                new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3)));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.calculerRevenu(1, "2026-13"))
                .isInstanceOf(MoisInvalideException.class);
        assertThatThrownBy(() -> service.calculerRevenu(1, "11/2026"))
                .isInstanceOf(MoisInvalideException.class);
    }
}

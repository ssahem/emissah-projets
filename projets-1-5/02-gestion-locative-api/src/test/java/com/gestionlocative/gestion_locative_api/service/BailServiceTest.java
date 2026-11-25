package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.dto.request.CreateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RenouvellementBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.ResiliationBailRequest;
import com.gestionlocative.gestion_locative_api.dto.request.RevisionLoyerRequest;
import com.gestionlocative.gestion_locative_api.dto.request.UpdateBailRequest;
import com.gestionlocative.gestion_locative_api.dto.response.BailResponse;
import com.gestionlocative.gestion_locative_api.dto.response.PageResponse;
import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.entity.HistoriqueBail;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.BailChevauchementException;
import com.gestionlocative.gestion_locative_api.exception.BailDateFinInvalideException;
import com.gestionlocative.gestion_locative_api.exception.BailModificationInterditeException;
import com.gestionlocative.gestion_locative_api.exception.BailNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.BailStatutInvalideException;
import com.gestionlocative.gestion_locative_api.exception.DateEffetInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RenouvellementDateInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerDateInvalideException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerDejaExistanteException;
import com.gestionlocative.gestion_locative_api.exception.RevisionLoyerMontantInvalideException;
import com.gestionlocative.gestion_locative_api.mapper.BailMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailLocataireRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.HistoriqueBailRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires du service des baux (F5, F10 à F15).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des baux (F5, F10 à F15).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, horloge fixe au 15 juin 2026, mapper réel.
 * CAS COUVERTS : chevauchement, dates, résiliation, renouvellement, révision, suppression.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class BailServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 6, 15);

    @Mock private BailRepository bailRepository;
    @Mock private BailLocataireRepository bailLocataireRepository;
    @Mock private AppartementRepository appartementRepository;
    @Mock private LocataireRepository locataireRepository;
    @Mock private RevisionLoyerRepository revisionLoyerRepository;
    @Mock private HistoriqueBailRepository historiqueBailRepository;

    private BailService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        Clock horlogeFixe = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        service = new BailService(bailRepository, bailLocataireRepository,
                appartementRepository, locataireRepository, revisionLoyerRepository,
                historiqueBailRepository, new BailMapper(), horlogeFixe);
    }

    // ── locataire() ──────────────────────────────────────────────────────────

    /**
     * Prépare un bail de test avec des valeurs nominales.
     */

    private Locataire locataire() {
        Locataire locataire = new Locataire("Traore", "Kofi", "kofi@example.test", "0102");
        ReflectionTestUtils.setField(locataire, "idLocataire", 1);
        return locataire;
    }

    // ── bailCommence() ───────────────────────────────────────────────────────

    /**
     * Prépare le contexte nécessaire à l’opération bailCommence().
     */

    private Bail bailCommence() {
        Bail bail = new Bail(LocalDate.of(2026, 6, 1), null, BigDecimal.ZERO,
                BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(bail, "idBail", 5);
        return bail;
    }

    // ── creation() ───────────────────────────────────────────────────────────

    /**
     * Prépare un bail de test avec des valeurs nominales.
     */

    private CreateBailRequest creation(LocalDate debut, LocalDate fin) {
        return new CreateBailRequest(1, 1, debut, fin, new BigDecimal("850.00"), null, null);
    }

    // ── create_throwsWhenChevauchement() ─────────────────────────────────────

    @Test
    @DisplayName("La création refuse un chevauchement de périodes")
    void create_throwsWhenChevauchement() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(appartementRepository.existsById(1)).thenReturn(true);
        when(locataireRepository.findById(1)).thenReturn(Optional.of(locataire()));
        when(bailRepository.findChevauchements(any(), any(), any()))
                .thenReturn(List.of(bailCommence()));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(
                creation(LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30))))
                .isInstanceOf(BailChevauchementException.class);
    }

    // ── create_throwsWhenDateFinInvalide() ───────────────────────────────────

    @Test
    @DisplayName("La création refuse une date de fin moins d'un mois après le début")
    void create_throwsWhenDateFinInvalide() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(appartementRepository.existsById(1)).thenReturn(true);
        when(locataireRepository.findById(1)).thenReturn(Optional.of(locataire()));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(
                creation(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15))))
                .isInstanceOf(BailDateFinInvalideException.class);
    }

    // ── update_throwsWhenLoyerApresDebut() ───────────────────────────────────

    @Test
    @DisplayName("Le loyer d'un bail commencé ne peut plus être modifié")
    void update_throwsWhenLoyerApresDebut() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(5,
                new UpdateBailRequest(Optional.empty(), new BigDecimal("900.00"))))
                .isInstanceOf(BailModificationInterditeException.class);
    }

    // ── delete_throwsWhenBailCommence() ──────────────────────────────────────

    @Test
    @DisplayName("La suppression refuse un bail déjà commencé")
    void delete_throwsWhenBailCommence() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(5))
                .isInstanceOf(BailNonSupprimableException.class);
    }

    // ── findAll_renvoiePagePlafonnee() ───────────────────────────────────────

    @Test
    @DisplayName("F10 — la liste renvoie une page avec le total et plafonne la taille")
    void findAll_renvoiePagePlafonnee() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(bailCommence()),
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "dateDebut")), 137));

        // ── Action ───────────────────────────────────────────────────────────
        PageResponse<?> page = service.findAll(
                PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "dateDebut")));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.totalElements()).isEqualTo(137);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content()).hasSize(1);
    }

    // ── resilier_rameneDateFinEtTrace() ──────────────────────────────────────

    @Test
    @DisplayName("F13 — la résiliation raccourcit le bail et alimente l'historique")
    void resilier_rameneDateFinEtTrace() {
        // ── Préparation ──────────────────────────────────────────────────────
        Bail bail = new Bail(LocalDate.of(2026, 6, 1), LocalDate.of(2027, 5, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(bail, "idBail", 5);
        when(bailRepository.findById(5)).thenReturn(Optional.of(bail));
        when(bailLocataireRepository.findByIdIdBail(5)).thenReturn(List.of());

        // ── Action ───────────────────────────────────────────────────────────
        BailResponse resultat = service.resilier(
                5, new ResiliationBailRequest(LocalDate.of(2026, 9, 30)));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.statut()).isEqualTo(StatutBail.RESILIE);
        assertThat(resultat.dateFin()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(resultat.dateResiliation()).isEqualTo(LocalDate.of(2026, 9, 30));
        verify(historiqueBailRepository).saveAndFlush(any(HistoriqueBail.class));
    }

    // ── resilier_throwsWhenDatePasse() ───────────────────────────────────────

    @Test
    @DisplayName("F13 — une date d'effet antérieure à aujourd'hui est refusée")
    void resilier_throwsWhenDatePasse() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.resilier(
                5, new ResiliationBailRequest(LocalDate.of(2026, 6, 14))))
                .isInstanceOf(DateEffetInvalideException.class);
    }

    // ── resilier_throwsWhenBailFutur() ───────────────────────────────────────

    @Test
    @DisplayName("F13 — un bail qui n'a pas commencé ne peut pas être résilié")
    void resilier_throwsWhenBailFutur() {
        // ── Préparation ──────────────────────────────────────────────────────
        Bail futur = new Bail(LocalDate.of(2026, 7, 1), null, BigDecimal.ZERO,
                BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(futur, "idBail", 6);
        when(bailRepository.findById(6)).thenReturn(Optional.of(futur));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.resilier(
                6, new ResiliationBailRequest(LocalDate.of(2026, 7, 15))))
                .isInstanceOf(BailStatutInvalideException.class);
    }

    // ── renouveler_reactiveBailTermine() ─────────────────────────────────────

    @Test
    @DisplayName("F14 — le renouvellement réactive un bail clôturé")
    void renouveler_reactiveBailTermine() {
        // ── Préparation ──────────────────────────────────────────────────────
        Bail termine = new Bail(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(termine, "idBail", 5);
        termine.cloturer(LocalDate.of(2026, 6, 2));
        when(bailRepository.findById(5)).thenReturn(Optional.of(termine));
        when(bailRepository.findChevauchements(any(), any(), any())).thenReturn(List.of());
        when(bailLocataireRepository.findByIdIdBail(5)).thenReturn(List.of());

        // ── Action ───────────────────────────────────────────────────────────
        BailResponse resultat = service.renouveler(
                5, new RenouvellementBailRequest(LocalDate.of(2027, 5, 31)));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.statut()).isEqualTo(StatutBail.EN_COURS);
        assertThat(resultat.dateFin()).isEqualTo(LocalDate.of(2027, 5, 31));
        assertThat(resultat.dateCloture()).isNull();
        verify(historiqueBailRepository).saveAndFlush(any(HistoriqueBail.class));
    }

    // ── renouveler_throwsWhenChevauchement() ─────────────────────────────────

    @Test
    @DisplayName("F14 — la période ajoutée ne doit pas chevaucher un autre bail")
    void renouveler_throwsWhenChevauchement() {
        // ── Préparation ──────────────────────────────────────────────────────
        Bail bail = new Bail(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, new BigDecimal("850.00"));
        ReflectionTestUtils.setField(bail, "idBail", 5);
        Bail bloquant = new Bail(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31),
                BigDecimal.ZERO, BigDecimal.ZERO, 1, new BigDecimal("900.00"));
        ReflectionTestUtils.setField(bloquant, "idBail", 9);
        when(bailRepository.findById(5)).thenReturn(Optional.of(bail));
        when(bailRepository.findChevauchements(any(), any(), any()))
                .thenReturn(List.of(bloquant));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.renouveler(
                5, new RenouvellementBailRequest(LocalDate.of(2027, 6, 30))))
                .isInstanceOf(BailChevauchementException.class);
    }

    // ── reviserLoyer_metAJourLoyer() ─────────────────────────────────────────

    @Test
    @DisplayName("F15 — une révision met à jour le loyer courant")
    void reviserLoyer_metAJourLoyer() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));
        when(bailLocataireRepository.findByIdIdBail(5)).thenReturn(List.of());
        when(revisionLoyerRepository.existsByIdBailAndDateEffet(5, LocalDate.of(2027, 1, 1)))
                .thenReturn(false);
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(5))
                .thenReturn(List.of(new RevisionLoyer(
                        5, LocalDate.of(2026, 6, 1), new BigDecimal("850.00"))));

        // ── Action ───────────────────────────────────────────────────────────
        BailResponse resultat = service.reviserLoyer(5, new RevisionLoyerRequest(
                new BigDecimal("920.00"), LocalDate.of(2027, 1, 1)));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.loyerMensuel()).isEqualByComparingTo("920.00");
        verify(revisionLoyerRepository).saveAndFlush(any(RevisionLoyer.class));
    }

    // ── reviserLoyer_throwsWhenDateDejaUtilisee() ────────────────────────────

    @Test
    @DisplayName("F15 — une révision déjà présente à cette date renvoie 409")
    void reviserLoyer_throwsWhenDateDejaUtilisee() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));
        when(revisionLoyerRepository.existsByIdBailAndDateEffet(5, LocalDate.of(2026, 6, 1)))
                .thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.reviserLoyer(5, new RevisionLoyerRequest(
                new BigDecimal("920.00"), LocalDate.of(2026, 6, 1))))
                .isInstanceOf(RevisionLoyerDejaExistanteException.class);
    }

    // ── reviserLoyer_throwsWhenDateAnterieure() ──────────────────────────────

    @Test
    @DisplayName("F15 — une révision antérieure à la dernière est refusée")
    void reviserLoyer_throwsWhenDateAnterieure() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(bailRepository.findById(5)).thenReturn(Optional.of(bailCommence()));
        when(revisionLoyerRepository.existsByIdBailAndDateEffet(5, LocalDate.of(2026, 12, 1)))
                .thenReturn(false);
        when(revisionLoyerRepository.findByIdBailOrderByDateEffetDesc(5))
                .thenReturn(List.of(new RevisionLoyer(
                        5, LocalDate.of(2027, 1, 1), new BigDecimal("900.00"))));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.reviserLoyer(5, new RevisionLoyerRequest(
                new BigDecimal("920.00"), LocalDate.of(2026, 12, 1))))
                .isInstanceOf(RevisionLoyerDateInvalideException.class);
    }

    // ── reviserLoyer_throwsWhenMontantInvalide() ─────────────────────────────

    @Test
    @DisplayName("F15 — un montant nul est refusé")
    void reviserLoyer_throwsWhenMontantInvalide() {
        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.reviserLoyer(5, new RevisionLoyerRequest(
                BigDecimal.ZERO, LocalDate.of(2027, 1, 1))))
                .isInstanceOf(RevisionLoyerMontantInvalideException.class);
    }
}

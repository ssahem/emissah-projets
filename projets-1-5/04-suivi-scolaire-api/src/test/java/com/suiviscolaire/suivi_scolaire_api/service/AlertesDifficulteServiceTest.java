package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.entity.AlerteDifficulte;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.repository.AlerteDifficulteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.VueRepository;
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
 * Détection hebdomadaire des difficultés (F16, arbitrage A-05).
 *
 * COUCHE       : Service
 * RÔLE         : Détection hebdomadaire des difficultés (F16, arbitrage A-05).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class AlertesDifficulteServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 11, 24);

    @Mock private VueRepository vueRepository;
    @Mock private AlerteDifficulteRepository alerteRepository;
    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private TrimestreRepository trimestreRepository;
    @Mock private EleveRepository eleveRepository;
    @Mock private ClasseRepository classeRepository;

    private AlertesDifficulteService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new AlertesDifficulteService(vueRepository, alerteRepository,
                inscriptionRepository, trimestreRepository, eleveRepository, classeRepository,
                CLOCK, "0 0 1 * * MON");
    }

    // ── detecteMoyenneFaible() ───────────────────────────────────────────────

    @Test
    @DisplayName("F16 : une moyenne inférieure à 8 crée une alerte")
    void detecteMoyenneFaible() {
        prepTrimestreEnCours();
        when(inscriptionRepository.findAll()).thenReturn(List.of(inscription()));
        when(vueRepository.moyenneGenerale(1, 2)).thenReturn(new BigDecimal("7.50"));
        when(vueRepository.moyenneGenerale(1, 1)).thenReturn(new BigDecimal("9.50"));
        when(alerteRepository.findByIdInscriptionAndNumeroTrimestre(1, 2)).thenReturn(Optional.empty());
        when(alerteRepository.save(any(AlerteDifficulte.class))).thenAnswer(invocation -> {
            AlerteDifficulte a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "idAlerte", 7);
            return a;
        });
        prepReferences();

        var rapport = service.detecter();

        assertThat(rapport.alertesCreees()).isEqualTo(1);
        assertThat(rapport.trimestre()).isEqualTo(2);
        assertThat(rapport.alertes().get(0).motif()).contains("Moyenne 7.50 inférieure à 8");
        assertThat(rapport.alertes().get(0).nomEleve()).isEqualTo("Kouassi");
    }

    // ── detecteChuteForte() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : une chute de plus de 4 points crée une alerte")
    void detecteChuteForte() {
        prepTrimestreEnCours();
        when(inscriptionRepository.findAll()).thenReturn(List.of(inscription()));
        when(vueRepository.moyenneGenerale(1, 2)).thenReturn(new BigDecimal("12.00"));
        when(vueRepository.moyenneGenerale(1, 1)).thenReturn(new BigDecimal("17.00"));
        when(alerteRepository.findByIdInscriptionAndNumeroTrimestre(1, 2)).thenReturn(Optional.empty());
        when(alerteRepository.save(any(AlerteDifficulte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        prepReferences();

        var rapport = service.detecter();

        assertThat(rapport.alertesCreees()).isEqualTo(1);
        assertThat(rapport.alertes().get(0).motif()).contains("Chute de 5.00 points");
        assertThat(rapport.alertes().get(0).ecart()).isEqualByComparingTo("5.00");
    }

    // ── neCreeRienSansDifficulte() ───────────────────────────────────────────

    @Test
    @DisplayName("F16 : aucune alerte si la moyenne est stable et supérieure à 8")
    void neCreeRienSansDifficulte() {
        prepTrimestreEnCours();
        when(inscriptionRepository.findAll()).thenReturn(List.of(inscription()));
        when(vueRepository.moyenneGenerale(1, 2)).thenReturn(new BigDecimal("12.00"));
        when(vueRepository.moyenneGenerale(1, 1)).thenReturn(new BigDecimal("14.00"));

        var rapport = service.detecter();

        assertThat(rapport.alertesCreees()).isZero();
        verify(alerteRepository, never()).save(any());
    }

    // ── neDupliquePasAlerte() ────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : une alerte déjà présente n'est pas dupliquée")
    void neDupliquePasAlerte() {
        prepTrimestreEnCours();
        when(inscriptionRepository.findAll()).thenReturn(List.of(inscription()));
        when(vueRepository.moyenneGenerale(1, 2)).thenReturn(new BigDecimal("7.00"));
        when(vueRepository.moyenneGenerale(1, 1)).thenReturn(new BigDecimal("9.00"));
        when(alerteRepository.findByIdInscriptionAndNumeroTrimestre(1, 2))
                .thenReturn(Optional.of(alerte()));

        var rapport = service.detecter();

        assertThat(rapport.alertesCreees()).isZero();
        assertThat(rapport.alertesDejaPresentes()).isEqualTo(1);
        verify(alerteRepository, never()).save(any());
    }

    // ── aucunTrimestreEnCours() ──────────────────────────────────────────────

    @Test
    @DisplayName("F16 : la détection est ignorée hors d'un trimestre en cours")
    void aucunTrimestreEnCours() {
        when(trimestreRepository.findContenant(AUJOURDHUI)).thenReturn(List.of());

        var rapport = service.detecter();

        assertThat(rapport.trimestre()).isNull();
        assertThat(rapport.alertesCreees()).isZero();
        verify(inscriptionRepository, never()).findAll();
    }

    // ── consulteAlertes() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F16 : la consultation expose les alertes persistées")
    void consulteAlertes() {
        when(alerteRepository.findAllByNumeroTrimestreOrderByIdAlerteAsc(2))
                .thenReturn(List.of(alerte()));
        when(inscriptionRepository.findById(1)).thenReturn(Optional.of(inscription()));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve()));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));

        var alertes = service.consulter(2);

        assertThat(alertes).singleElement().satisfies(a -> {
            assertThat(a.trimestre()).isEqualTo(2);
            assertThat(a.nomClasse()).isEqualTo("6A");
            assertThat(a.moyenne()).isEqualByComparingTo("7.00");
        });
    }

    // ── consulteTrimestreInvalide() ──────────────────────────────────────────

    @Test
    @DisplayName("F16 : un numéro de trimestre hors de 1 à 3 est refusé")
    void consulteTrimestreInvalide() {
        assertThatThrownBy(() -> service.consulter(4))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("entre 1 et 3");
    }


    // ── prepTrimestreEnCours() ───────────────────────────────────────────────

    /**
     * Prépare le trimestre contenant la date de l'horloge fixe.
     */

    private void prepTrimestreEnCours() {
        Trimestre t = new Trimestre(2, LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 2, 28), 2);
        when(trimestreRepository.findContenant(AUJOURDHUI)).thenReturn(List.of(t));
    }


    // ── prepReferences() ─────────────────────────────────────────────────────

    /**
     * Prépare les références utilisées pour enrichir la réponse.
     */

    private void prepReferences() {
        when(inscriptionRepository.findById(1)).thenReturn(Optional.of(inscription()));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve()));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));
    }


    // ── inscription() ────────────────────────────────────────────────────────

    /**
     * Construit une inscription courante.
     */

    private Inscription inscription() {
        Inscription i = new Inscription(LocalDate.of(2026, 9, 2), 1, 1, "INS-2026-001");
        ReflectionTestUtils.setField(i, "idInscription", 1);
        return i;
    }


    // ── alerte() ─────────────────────────────────────────────────────────────

    /**
     * Construit une alerte déjà persistée.
     */

    private AlerteDifficulte alerte() {
        AlerteDifficulte a = new AlerteDifficulte(1, 2, new BigDecimal("7.00"),
                new BigDecimal("2.00"), AUJOURDHUI);
        ReflectionTestUtils.setField(a, "idAlerte", 7);
        return a;
    }


    // ── eleve() ──────────────────────────────────────────────────────────────

    /**
     * Construit un élève.
     */

    private Eleve eleve() {
        Eleve e = new Eleve("Kouassi", "Aya", LocalDate.of(2011, 2, 14));
        ReflectionTestUtils.setField(e, "idEleve", 1);
        return e;
    }


    // ── classe() ─────────────────────────────────────────────────────────────

    /**
     * Construit une classe.
     */

    private Classe classe() {
        Classe c = new Classe("6A", 2, 1, 30);
        ReflectionTestUtils.setField(c, "idClasse", 1);
        return c;
    }
}

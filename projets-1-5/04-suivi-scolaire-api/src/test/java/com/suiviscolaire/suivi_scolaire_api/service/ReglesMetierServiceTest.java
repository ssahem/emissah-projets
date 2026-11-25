package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.HistoriqueReouverture;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.entity.TypeEvaluation;
import com.suiviscolaire.suivi_scolaire_api.exception.ApiException;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.HistoriqueReouvertureRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TypeEvaluationRepository;
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
 * Règles métier F13 à F15 (poids, clôture et réouverture).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F13 à F15 (poids, clôture et réouverture).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ReglesMetierServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
    private static final String MOTIF = "Correction d'une erreur de saisie avérée";

    @Mock private TypeEvaluationRepository typeEvaluationRepository;
    @Mock private TrimestreRepository trimestreRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private ClasseRepository classeRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private HistoriqueReouvertureRepository historiqueRepository;

    private ReglesMetierService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new ReglesMetierService(typeEvaluationRepository, trimestreRepository,
                enseignementRepository, noteRepository, classeRepository, matiereRepository,
                historiqueRepository, new TrimestreLookup(trimestreRepository), CLOCK);
    }

    // ── modifierPoidsParAlias() ──────────────────────────────────────────────

    @Test
    @DisplayName("F13 : un alias DEV résout le type Devoir et met à jour son poids")
    void modifierPoidsParAlias() {
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(false);
        when(typeEvaluationRepository.findByLibelle("Devoir"))
                .thenReturn(Optional.of(type("Devoir", 1)));

        var response = service.modifierPoids("DEV", new BigDecimal("2.5"));

        assertThat(response.code()).isEqualTo("DEV");
        assertThat(response.libelle()).isEqualTo("Devoir");
        assertThat(response.poids()).isEqualByComparingTo("2.5");
    }

    // ── modifierPoidsParLibelle() ────────────────────────────────────────────

    @Test
    @DisplayName("F13 : le libellé est accepté quelle que soit la casse")
    void modifierPoidsParLibelle() {
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(false);
        when(typeEvaluationRepository.findByLibelleIgnoreCase("interrogation"))
                .thenReturn(Optional.of(type("Interrogation", 2)));

        assertThat(service.modifierPoids("interrogation", BigDecimal.TWO).libelle())
                .isEqualTo("Interrogation");
    }

    // ── modifierPoidsNull() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F13 : un poids nul est refusé avant toute écriture")
    void modifierPoidsNull() {
        assertThatThrownBy(() -> service.modifierPoids("DEV", null))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("strictement positif");
        verify(typeEvaluationRepository, never()).findByLibelle(any());
    }

    // ── modifierPoidsApresCloture() ──────────────────────────────────────────

    @Test
    @DisplayName("F13 : un trimestre clôturé fige tous les poids")
    void modifierPoidsApresCloture() {
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(true);

        assertThatThrownBy(() -> service.modifierPoids("DEV", BigDecimal.TWO))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("plus modifiable");
    }

    // ── modifierPoidsCodeInconnu() ───────────────────────────────────────────

    @Test
    @DisplayName("F13 : un code inconnu est refusé")
    void modifierPoidsCodeInconnu() {
        when(trimestreRepository.existsByEstClotureTrue()).thenReturn(false);

        assertThatThrownBy(() -> service.modifierPoids("ZZZ", BigDecimal.TWO))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("inconnu");
    }

    // ── cloturerComplet() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : la clôture est acceptée si chaque enseignement a une note")
    void cloturerComplet() {
        Trimestre trimestre = trimestre(1, false);
        when(trimestreRepository.findByNumero(1)).thenReturn(Optional.of(trimestre));
        when(enseignementRepository.findAll()).thenReturn(List.of(enseignement(5)));
        when(noteRepository.existsByIdEnseignementAndTrimestre(5, 1)).thenReturn(true);

        var response = service.cloturer(1);

        assertThat(response.dateCloture()).isEqualTo(LocalDate.of(2026, 11, 24));
        assertThat(response.nombreEnseignementsVerifies()).isEqualTo(1);
        assertThat(trimestre.isEstCloture()).isTrue();
    }

    // ── cloturerIncomplet() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : la clôture incomplète renvoie 409 avec les classes et matières manquantes")
    void cloturerIncomplet() {
        when(trimestreRepository.findByNumero(1)).thenReturn(Optional.of(trimestre(1, false)));
        when(enseignementRepository.findAll()).thenReturn(List.of(enseignement(5)));
        when(noteRepository.existsByIdEnseignementAndTrimestre(5, 1)).thenReturn(false);
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A")));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques")));

        assertThatThrownBy(() -> service.cloturer(1))
                .isInstanceOf(ConflitMetierException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getProperties().get("manquants")).asList().hasSize(1);
                });
    }

    // ── cloturerDejaCloture() ────────────────────────────────────────────────

    @Test
    @DisplayName("F14 : un trimestre déjà clôturé ne peut pas l'être de nouveau")
    void cloturerDejaCloture() {
        when(trimestreRepository.findByNumero(1)).thenReturn(Optional.of(trimestre(1, true)));

        assertThatThrownBy(() -> service.cloturer(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà clôturé");
        verify(enseignementRepository, never()).findAll();
    }

    // ── reouvrirMotifCourt() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : un motif trop court est refusé")
    void reouvrirMotifCourt() {
        assertThatThrownBy(() -> service.reouvrir(3, "trop court", "Agent"))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("20 caractères");
    }

    // ── reouvrirDernier() ────────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : le dernier trimestre clôturé est rouvert et tracé")
    void reouvrirDernier() {
        Trimestre trimestre = trimestre(3, true);
        when(trimestreRepository.findByNumero(3)).thenReturn(Optional.of(trimestre));
        when(trimestreRepository.findCloturesDuPlusRecentAuPlusAncien())
                .thenReturn(List.of(trimestre));
        when(historiqueRepository.findByIdTrimestre(3)).thenReturn(Optional.empty());
        when(historiqueRepository.save(any(HistoriqueReouverture.class))).thenAnswer(invocation -> {
            HistoriqueReouverture h = invocation.getArgument(0);
            ReflectionTestUtils.setField(h, "idHistorique", 50);
            return h;
        });

        var response = service.reouvrir(3, MOTIF, "Secrétariat");

        assertThat(response.idHistorique()).isEqualTo(50);
        assertThat(response.motif()).isEqualTo(MOTIF);
        assertThat(response.auteur()).isEqualTo("Secrétariat");
        assertThat(response.dateReouverture()).isEqualTo(LocalDate.of(2026, 11, 24));
        assertThat(trimestre.isEstCloture()).isFalse();
    }

    // ── reouvrirNonDernier() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : seul le plus récent des trimestres clôturés peut être rouvert")
    void reouvrirNonDernier() {
        Trimestre dernier = trimestre(3, true);
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre(2, true)));
        when(trimestreRepository.findCloturesDuPlusRecentAuPlusAncien())
                .thenReturn(List.of(dernier));

        assertThatThrownBy(() -> service.reouvrir(2, MOTIF, "Agent"))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("dernier trimestre clôturé");
    }

    // ── reouvrirTrimestreOuvert() ────────────────────────────────────────────

    @Test
    @DisplayName("F15 : un trimestre ouvert ne peut pas être rouvert")
    void reouvrirTrimestreOuvert() {
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre(2, false)));

        assertThatThrownBy(() -> service.reouvrir(2, MOTIF, "Agent"))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("n'est pas clôturé");
    }

    // ── reouvrirDejaTrace() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F15 : une réouverture déjà tracée est refusée")
    void reouvrirDejaTrace() {
        Trimestre trimestre = trimestre(3, true);
        when(trimestreRepository.findByNumero(3)).thenReturn(Optional.of(trimestre));
        when(trimestreRepository.findCloturesDuPlusRecentAuPlusAncien())
                .thenReturn(List.of(trimestre));
        when(historiqueRepository.findByIdTrimestre(3))
                .thenReturn(Optional.of(new HistoriqueReouverture(
                        3, LocalDate.of(2026, 11, 20), MOTIF, "Agent")));

        assertThatThrownBy(() -> service.reouvrir(3, MOTIF, "Agent"))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà été rouvert");
        verify(historiqueRepository, never()).save(any());
    }

    /** Construit un trimestre avec son identifiant. */

    // ── trimestre() ──────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour trimestre().
     */

    private Trimestre trimestre(Integer numero, boolean cloture) {
        Trimestre t = new Trimestre(numero, LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 2, 28), 2);
        ReflectionTestUtils.setField(t, "idTrimestre", numero);
        if (cloture) {
            t.cloturer(LocalDate.of(2026, 11, 20));
        }
        return t;
    }

    /** Construit un enseignement avec son identifiant. */

    // ── enseignement() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour enseignement().
     */

    private Enseignement enseignement(Integer id) {
        Enseignement e = new Enseignement(1, 1, 1);
        ReflectionTestUtils.setField(e, "idEnseignement", id);
        return e;
    }

    /** Construit un type d'évaluation avec son identifiant. */

    // ── type() ───────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour type().
     */

    private TypeEvaluation type(String libelle, Integer id) {
        TypeEvaluation t = new TypeEvaluation(libelle, BigDecimal.ONE);
        ReflectionTestUtils.setField(t, "idTypeEval", id);
        return t;
    }

    /** Construit une classe avec son identifiant. */

    // ── classe() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour classe().
     */

    private Classe classe(Integer id, String nom) {
        Classe c = new Classe(nom, 2, 1, 30);
        ReflectionTestUtils.setField(c, "idClasse", id);
        return c;
    }

    /** Construit une matière avec son identifiant. */

    // ── matiere() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour matiere().
     */

    private Matiere matiere(Integer id, String libelle) {
        Matiere m = new Matiere(libelle, "MAT", BigDecimal.ONE);
        ReflectionTestUtils.setField(m, "idMatiere", id);
        return m;
    }
}

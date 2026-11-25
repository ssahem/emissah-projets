package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.VueRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Restitution des moyennes et du classement (F17 à F19).
 *
 * COUCHE       : Service
 * RÔLE         : Restitution des moyennes et du classement (F17 à F19).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class RestitutionServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private VueRepository vueRepository;
    @Mock private EleveRepository eleveRepository;
    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private ClasseRepository classeRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private TrimestreRepository trimestreRepository;

    private RestitutionService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new RestitutionService(vueRepository, eleveRepository, inscriptionRepository,
                classeRepository, enseignementRepository, matiereRepository, noteRepository,
                new TrimestreLookup(trimestreRepository));
    }

    // ── moyennesAvecMatiereNonEvaluee() ──────────────────────────────────────

    @Test
    @DisplayName("F17 : une matière sans note est retournée avec moyenne null et evaluee=false")
    void moyennesAvecMatiereNonEvaluee() {
        prepEleveInscriptionTrimestre();
        when(vueRepository.moyennesMatiere(10, 2)).thenReturn(List.of(
                Map.of("id_enseignement", 5, "moyenne_matiere", new BigDecimal("12.50"))));
        when(enseignementRepository.findAllByIdClasseOrderByIdEnseignementAsc(1))
                .thenReturn(List.of(enseignement(5, 1), enseignement(6, 2)));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques", BigDecimal.valueOf(4))));
        when(matiereRepository.findById(2))
                .thenReturn(Optional.of(matiere(2, "Francais", BigDecimal.valueOf(3))));
        when(noteRepository.countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(5, 2, "INS-2026-10")).thenReturn(2L);
        when(noteRepository.countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(6, 2, "INS-2026-10")).thenReturn(0L);

        var response = service.moyennesParMatiere(1, 2);

        assertThat(response.matieres()).hasSize(2);
        assertThat(response.matieres().get(0).moyenne()).isEqualByComparingTo("12.50");
        assertThat(response.matieres().get(0).nombreNotes()).isEqualTo(2);
        assertThat(response.matieres().get(0).evaluee()).isTrue();
        assertThat(response.matieres().get(1).moyenne()).isNull();
        assertThat(response.matieres().get(1).evaluee()).isFalse();
    }

    // ── moyennesEleveInconnu() ───────────────────────────────────────────────

    @Test
    @DisplayName("F17 : un élève inconnu renvoie 404")
    void moyennesEleveInconnu() {
        when(eleveRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moyennesParMatiere(9, 2))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── moyennesSansInscription() ────────────────────────────────────────────

    @Test
    @DisplayName("F17 : une inscription courante manquante est signalée")
    void moyennesSansInscription() {
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moyennesParMatiere(1, 2))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("aucune inscription courante");
    }

    // ── moyenneGeneraleDetaillee() ───────────────────────────────────────────

    @Test
    @DisplayName("F18 : la moyenne générale est pondérée et détaillée")
    void moyenneGeneraleDetaillee() {
        prepEleveInscriptionTrimestre();
        when(vueRepository.moyenneGenerale(10, 2)).thenReturn(new BigDecimal("13.20"));
        when(vueRepository.moyennesMatiere(10, 2)).thenReturn(List.of(
                Map.of("id_enseignement", 5, "moyenne_matiere", new BigDecimal("12.50"))));
        when(enseignementRepository.findAllByIdClasseOrderByIdEnseignementAsc(1))
                .thenReturn(List.of(enseignement(5, 1)));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques", BigDecimal.valueOf(4))));
        when(noteRepository.countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(5, 2, "INS-2026-10")).thenReturn(2L);

        var response = service.moyenneGenerale(1, 2);

        assertThat(response.moyenneGenerale()).isEqualByComparingTo("13.20");
        assertThat(response.detail()).singleElement().satisfies(d -> {
            assertThat(d.libelleMatiere()).isEqualTo("Mathematiques");
            assertThat(d.contribution()).isEqualByComparingTo("50.00");
        });
    }

    // ── moyenneGeneraleSansNote() ────────────────────────────────────────────

    @Test
    @DisplayName("F18 : sans aucune note, la moyenne est absente et non égale à zéro")
    void moyenneGeneraleSansNote() {
        prepEleveInscriptionTrimestre();
        when(vueRepository.moyenneGenerale(10, 2)).thenReturn(null);
        when(vueRepository.moyennesMatiere(10, 2)).thenReturn(List.of());
        when(enseignementRepository.findAllByIdClasseOrderByIdEnseignementAsc(1))
                .thenReturn(List.of(enseignement(6, 2)));
        when(matiereRepository.findById(2))
                .thenReturn(Optional.of(matiere(2, "Francais", BigDecimal.valueOf(3))));
        when(noteRepository.countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(6, 2, "INS-2026-10")).thenReturn(0L);

        var response = service.moyenneGenerale(1, 2);

        assertThat(response.moyenneGenerale()).isNull();
        assertThat(response.detail()).isEmpty();
    }

    // ── classementAvecExAequo() ──────────────────────────────────────────────

    @Test
    @DisplayName("F19 : le classement gère ex æquo et élèves sans note en fin de liste")
    void classementAvecExAequo() {
        when(classeRepository.findById(1))
                .thenReturn(Optional.of(classe()));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
        when(vueRepository.rangsClasse(1, 2)).thenReturn(List.of(
                Map.of("id_inscription", 10, "moyenne_generale", new BigDecimal("14.00"), "rang", 1),
                Map.of("id_inscription", 11, "moyenne_generale", new BigDecimal("14.00"), "rang", 1)));
        when(inscriptionRepository.findAllByIdClasseAndDateFinIsNull(1))
                .thenReturn(List.of(inscription(10, 1), inscription(11, 2), inscription(12, 3)));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(eleveRepository.findById(2)).thenReturn(Optional.of(eleve(2)));
        when(eleveRepository.findById(3)).thenReturn(Optional.of(eleve(3)));

        var response = service.classement(1, 2);

        assertThat(response.eleves()).hasSize(3);
        assertThat(response.eleves().get(0).rang()).isEqualTo(1);
        assertThat(response.eleves().get(1).rang()).isEqualTo(1);
        assertThat(response.eleves().get(2).rang()).isNull();
        assertThat(response.eleves().get(2).moyenneGenerale()).isNull();
        assertThat(response.moyenneClasse()).isEqualByComparingTo("14.00");
    }

    // ── classementClasseInconnue() ───────────────────────────────────────────

    @Test
    @DisplayName("F19 : une classe inconnue renvoie 404")
    void classementClasseInconnue() {
        when(classeRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.classement(9, 2))
                .isInstanceOf(RessourceIntrouvableException.class);
    }


    // ── prepEleveInscriptionTrimestre() ──────────────────────────────────────

    /**
     * Prépare un élève, son inscription courante et le trimestre.
     */

    private void prepEleveInscriptionTrimestre() {
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(10, 1)));
    }


    // ── eleve() ──────────────────────────────────────────────────────────────

    /**
     * Construit un élève avec son identifiant.
     */

    private Eleve eleve(Integer id) {
        Eleve e = new Eleve("Kouassi-" + id, "Aya", LocalDate.of(2011, 2, 14));
        ReflectionTestUtils.setField(e, "idEleve", id);
        return e;
    }


    // ── inscription() ────────────────────────────────────────────────────────

    /**
     * Construit une inscription avec son identifiant.
     */

    private Inscription inscription(Integer id, Integer idEleve) {
        Inscription i = new Inscription(LocalDate.of(2026, 9, 2), idEleve, 1, "INS-2026-" + id);
        ReflectionTestUtils.setField(i, "idInscription", id);
        return i;
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


    // ── trimestre() ──────────────────────────────────────────────────────────

    /**
     * Construit un trimestre ouvert.
     */

    private Trimestre trimestre() {
        Trimestre t = new Trimestre(2, LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 2, 28), 2);
        ReflectionTestUtils.setField(t, "idTrimestre", 2);
        return t;
    }


    // ── enseignement() ───────────────────────────────────────────────────────

    /**
     * Construit un enseignement avec son identifiant.
     */

    private Enseignement enseignement(Integer id, Integer idMatiere) {
        Enseignement e = new Enseignement(1, idMatiere, 1);
        ReflectionTestUtils.setField(e, "idEnseignement", id);
        return e;
    }


    // ── matiere() ────────────────────────────────────────────────────────────

    /**
     * Construit une matière avec son identifiant.
     */

    private Matiere matiere(Integer id, String libelle, BigDecimal coefficient) {
        Matiere m = new Matiere(libelle, "MAT", coefficient);
        ReflectionTestUtils.setField(m, "idMatiere", id);
        return m;
    }
}

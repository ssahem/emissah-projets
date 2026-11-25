package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.entity.TypeEvaluation;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.exception.RessourceIntrouvableException;
import com.suiviscolaire.suivi_scolaire_api.mapper.EleveMapper;
import com.suiviscolaire.suivi_scolaire_api.mapper.NoteMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.NoteRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TrimestreRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.TypeEvaluationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Consultations, recherche et pagination (F6 à F10).
 *
 * COUCHE       : Service
 * RÔLE         : Consultations, recherche et pagination (F6 à F10).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ClasseRepository classeRepository;
    @Mock private EleveRepository eleveRepository;
    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private TypeEvaluationRepository typeRepository;
    @Mock private TrimestreRepository trimestreRepository;

    private ConsultationService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        EleveMapper eleveMapper = new EleveMapper(inscriptionRepository, classeRepository);
        NoteMapper noteMapper = new NoteMapper(inscriptionRepository, enseignementRepository,
                matiereRepository, typeRepository, classeRepository, trimestreRepository);
        service = new ConsultationService(classeRepository, eleveRepository, inscriptionRepository,
                enseignementRepository, matiereRepository, noteRepository, eleveMapper, noteMapper,
                new TrimestreLookup(trimestreRepository));
    }

    // ── elevesDeClasseTries() ────────────────────────────────────────────────

    @Test
    @DisplayName("F6 : les élèves d'une classe sont triés par nom avec l'effectif courant")
    void elevesDeClasseTries() {
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));
        when(inscriptionRepository.findAllByIdClasseAndDateFinIsNull(1))
                .thenReturn(List.of(inscription(10, 1), inscription(11, 2)));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1, "Zozo")));
        when(eleveRepository.findById(2)).thenReturn(Optional.of(eleve(2, "Aya")));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(10, 1)));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(2))
                .thenReturn(Optional.of(inscription(11, 2)));

        var response = service.elevesDeClasse(1);

        assertThat(response.effectifActuel()).isEqualTo(2);
        assertThat(response.eleves()).extracting("nom").containsExactly("Aya", "Zozo");
    }

    // ── notesEleveGroupees() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F7 : les notes d'un élève sont groupées par matière")
    void notesEleveGroupees() {
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1, "Kouassi")));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(10, 1)));
        when(noteRepository.findByNumeroInscription("INS-10", null))
                .thenReturn(List.of(note(20, 5), note(21, 6)));
        when(enseignementRepository.findById(6)).thenReturn(Optional.of(enseignement(6)));
        when(matiereRepository.findById(2)).thenReturn(Optional.of(matiere(2, "Francais")));
        prepNoteMapper();

        var groupes = service.notesEleve(1, null);

        assertThat(groupes).hasSize(2);
        assertThat(groupes.get(0).idMatiere()).isEqualTo(1);
        assertThat(groupes.get(0).notes()).hasSize(1);
        assertThat(groupes.get(1).libelleMatiere()).isEqualTo("Francais");
    }

    // ── notesEleveInconnu() ──────────────────────────────────────────────────

    @Test
    @DisplayName("F7 : un élève inconnu renvoie 404")
    void notesEleveInconnu() {
        when(eleveRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.notesEleve(9, null))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ── notesMatiereClasseAvecMoyenne() ──────────────────────────────────────

    @Test
    @DisplayName("F8 : les notes d'une matière sont groupées avec la moyenne de classe")
    void notesMatiereClasseAvecMoyenne() {
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques")));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1))
                .thenReturn(Optional.of(enseignement(5)));
        when(noteRepository.findByIdEnseignementAndTrimestre(5, 2))
                .thenReturn(List.of(noteAvecValeur(20, 5, "14.00"),
                        noteAvecValeur(21, 5, "16.00")));
        when(inscriptionRepository.findByNumeroInscription("INS-10"))
                .thenReturn(Optional.of(inscription(10, 1)));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1, "Kouassi")));
        prepNoteMapper();

        var response = service.notesMatiereClasse(1, 1, 2);

        assertThat(response.moyenneClasse()).isEqualByComparingTo("15.00");
        assertThat(response.eleves()).singleElement().satisfies(e -> {
            assertThat(e.nomEleve()).isEqualTo("Kouassi");
            assertThat(e.moyenne()).isEqualByComparingTo("15.00");
            assertThat(e.notes()).hasSize(2);
        });
    }

    // ── notesMatiereNonEnseignee() ───────────────────────────────────────────

    @Test
    @DisplayName("F8 : une matière non enseignée dans la classe est refusée")
    void notesMatiereNonEnseignee() {
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques")));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.notesMatiereClasse(1, 1, 2))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("pas enseignée");
    }

    // ── rechercheVide() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : une recherche vide est refusée")
    void rechercheVide() {
        assertThatThrownBy(() -> service.rechercher("  "))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("vide");
    }

    // ── recherchePartielle() ─────────────────────────────────────────────────

    @Test
    @DisplayName("F9 : la recherche partielle est insensible à la casse")
    void recherchePartielle() {
        when(eleveRepository.searchByNom("kOU")).thenReturn(List.of(eleve(1, "Kouassi")));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(10, 1)));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));

        assertThat(service.rechercher("kOU")).singleElement()
                .satisfies(e -> assertThat(e.nom()).isEqualTo("Kouassi"));
        verify(eleveRepository).searchByNom("kOU");
    }

    // ── notesPagineesTriees() ────────────────────────────────────────────────

    @Test
    @DisplayName("F10 : la pagination trie par valeur décroissante quand demandé")
    void notesPagineesTriees() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "valeur"));
        when(noteRepository.findAllBy(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(noteAvecValeur(20, 5, "18.00")), pageable, 5));

        var page = service.notesPaginees(0, 2, "valeur,desc");

        assertThat(page.content()).hasSize(1);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(noteRepository).findAllBy(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("valeur").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    /** Prépare les références nécessaires à NoteMapper. */

    // ── prepNoteMapper() ─────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour prepNoteMapper().
     */

    private void prepNoteMapper() {
        when(inscriptionRepository.findByNumeroInscription("INS-10"))
                .thenReturn(Optional.of(inscription(10, 1)));
        when(enseignementRepository.findById(5)).thenReturn(Optional.of(enseignement(5)));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe()));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(matiere(1, "Mathematiques")));
        when(typeRepository.findById(1))
                .thenReturn(Optional.of(new TypeEvaluation("Devoir", BigDecimal.ONE)));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestre()));
    }

    /** Construit une classe. */

    // ── classe() ─────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour classe().
     */

    private Classe classe() {
        Classe c = new Classe("6A", 2, 1, 30);
        ReflectionTestUtils.setField(c, "idClasse", 1);
        return c;
    }

    /** Construit un élève. */

    // ── eleve() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour eleve().
     */

    private Eleve eleve(Integer id, String nom) {
        Eleve e = new Eleve(nom, "Aya", LocalDate.of(2011, 2, 14));
        ReflectionTestUtils.setField(e, "idEleve", id);
        return e;
    }

    /** Construit une inscription courante. */

    // ── inscription() ────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour inscription().
     */

    private Inscription inscription(Integer id, Integer idEleve) {
        Inscription i = new Inscription(LocalDate.of(2026, 9, 2), idEleve, 1, "INS-10");
        ReflectionTestUtils.setField(i, "idInscription", id);
        return i;
    }

    /** Construit un enseignement. */

    // ── enseignement() ───────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour enseignement().
     */

    private Enseignement enseignement(Integer id) {
        Enseignement e = new Enseignement(1, id == 6 ? 2 : 1, 1);
        ReflectionTestUtils.setField(e, "idEnseignement", id);
        return e;
    }

    /** Construit une matière. */

    // ── matiere() ────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour matiere().
     */

    private Matiere matiere(Integer id, String libelle) {
        Matiere m = new Matiere(libelle, "MAT", BigDecimal.ONE);
        ReflectionTestUtils.setField(m, "idMatiere", id);
        return m;
    }

    /** Construit un trimestre. */

    // ── trimestre() ──────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour trimestre().
     */

    private Trimestre trimestre() {
        Trimestre t = new Trimestre(2, LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 2, 28), 2);
        ReflectionTestUtils.setField(t, "idTrimestre", 2);
        return t;
    }

    /** Construit une note de valeur 12. */

    // ── note() ───────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour note().
     */

    private Note note(Integer id, Integer idEnseignement) {
        return noteAvecValeur(id, idEnseignement, "12.00");
    }

    /** Construit une note avec une valeur explicite. */

    // ── noteAvecValeur() ─────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour noteAvecValeur().
     */

    private Note noteAvecValeur(Integer id, Integer idEnseignement, String valeur) {
        Note n = new Note(new BigDecimal(valeur), 2, 1, idEnseignement, "INS-10");
        ReflectionTestUtils.setField(n, "idNote", id);
        return n;
    }
}

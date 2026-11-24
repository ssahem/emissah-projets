package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateNoteRequest;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Règles métier F5 (notes, avec A-01 et A-03). */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private EleveRepository eleveRepository;
    @Mock private TypeEvaluationRepository typeEvaluationRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private ClasseRepository classeRepository;
    @Mock private TrimestreRepository trimestreRepository;

    private NoteService service;

    @BeforeEach
    void setUp() {
        NoteMapper mapper = new NoteMapper(inscriptionRepository, enseignementRepository,
                matiereRepository, typeEvaluationRepository, classeRepository, trimestreRepository);
        service = new NoteService(noteRepository, inscriptionRepository, enseignementRepository,
                eleveRepository, typeEvaluationRepository, matiereRepository,
                new TrimestreLookup(trimestreRepository), mapper);
    }

    @Test
    @DisplayName("La création refuse un trimestre clôturé")
    void createRefuseTrimestreCloture() {
        when(trimestreRepository.findByNumero(1)).thenReturn(Optional.of(trimestreCloture()));

        assertThatThrownBy(() -> service.create(new CreateNoteRequest(
                1, 1, BigDecimal.TEN, 1, 1)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("clôturé");
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("La création refuse un trimestre inconnu avec un 400")
    void createRefuseTrimestreInconnu() {
        assertThatThrownBy(() -> service.create(new CreateNoteRequest(
                1, 1, BigDecimal.TEN, 4, 1)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("entre 1 et 3");
    }

    @Test
    @DisplayName("La création résout l'élève via son numéro d'inscription (A-01)")
    void createResoutInscription() {
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestreOuvert()));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve()));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription()));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateNoteRequest(
                1, 1, BigDecimal.TEN, 2, 1)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("pas enseignée");
        verify(noteRepository, never()).save(any());
    }

    @Test
    @DisplayName("La création persiste la note avec le numéro d'inscription")
    void createNominal() {
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestreOuvert()));
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve()));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription()));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1))
                .thenReturn(Optional.of(enseignement()));
        when(typeEvaluationRepository.existsById(1)).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note n = invocation.getArgument(0);
            ReflectionTestUtils.setField(n, "idNote", 20);
            return n;
        });
        when(inscriptionRepository.findByNumeroInscription("INS-2026-001"))
                .thenReturn(Optional.of(inscription()));

        assertThat(service.create(new CreateNoteRequest(1, 1, BigDecimal.valueOf(14.5), 2, 1))
                .idNote()).isEqualTo(20);
    }

    @Test
    @DisplayName("La modification est refusée après clôture")
    void updateRefuseApresCloture() {
        Note note = note();
        when(noteRepository.findById(20)).thenReturn(Optional.of(note));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestreCloture()));

        assertThatThrownBy(() -> service.update(20, new UpdateNoteRequest(BigDecimal.TEN)))
                .isInstanceOf(ConflitMetierException.class);
    }

    @Test
    @DisplayName("La modification ne change que la valeur")
    void updateNominal() {
        Note note = note();
        when(noteRepository.findById(20)).thenReturn(Optional.of(note));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestreOuvert()));
        when(inscriptionRepository.findByNumeroInscription("INS-2026-001"))
                .thenReturn(Optional.of(inscription()));

        assertThat(service.update(20, new UpdateNoteRequest(BigDecimal.valueOf(18))).valeur())
                .isEqualByComparingTo("18");
        assertThat(note.getNumeroTrimestre()).isEqualTo(2);
    }

    @Test
    @DisplayName("La suppression est possible si le trimestre est ouvert")
    void deleteNominal() {
        Note note = note();
        when(noteRepository.findById(20)).thenReturn(Optional.of(note));
        when(trimestreRepository.findByNumero(2)).thenReturn(Optional.of(trimestreOuvert()));

        service.delete(20);

        verify(noteRepository).delete(note);
    }

    @Test
    @DisplayName("Une note inconnue renvoie une erreur 404 métier")
    void findByIdIntrouvable() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException.class);
    }

    /** Construit une note rattachée au trimestre 2. */
    private Note note() {
        Note n = new Note(BigDecimal.valueOf(14), 2, 1, 1, "INS-2026-001");
        ReflectionTestUtils.setField(n, "idNote", 20);
        return n;
    }

    /** Construit un élève. */
    private Eleve eleve() {
        Eleve e = new Eleve("Kouassi", "Aya", LocalDate.of(2011, 2, 14));
        ReflectionTestUtils.setField(e, "idEleve", 1);
        return e;
    }

    /** Construit une inscription courante. */
    private Inscription inscription() {
        Inscription i = new Inscription(LocalDate.of(2026, 9, 2), 1, 1, "INS-2026-001");
        ReflectionTestUtils.setField(i, "idInscription", 1);
        return i;
    }

    /** Construit un enseignement avec son identifiant. */
    private Enseignement enseignement() {
        Enseignement e = new Enseignement(1, 1, 1);
        ReflectionTestUtils.setField(e, "idEnseignement", 5);
        return e;
    }

    /** Construit un trimestre ouvert. */
    private Trimestre trimestreOuvert() {
        Trimestre t = new Trimestre(2, LocalDate.of(2026, 11, 16),
                LocalDate.of(2027, 2, 15), 2);
        ReflectionTestUtils.setField(t, "idTrimestre", 2);
        return t;
    }

    /** Construit un trimestre clôturé. */
    private Trimestre trimestreCloture() {
        Trimestre t = trimestreOuvert();
        t.cloturer(LocalDate.of(2026, 11, 20));
        return t;
    }
}

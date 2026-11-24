package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.dto.request.ChangeClasseRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.UpdateEleveRequest;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import com.suiviscolaire.suivi_scolaire_api.entity.Inscription;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.exception.DonneeInvalideException;
import com.suiviscolaire.suivi_scolaire_api.mapper.EleveMapper;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EleveRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.InscriptionRepository;
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

/** Règles métier F2 et F11 (élèves et changement de classe). */
@ExtendWith(MockitoExtension.class)
class EleveServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);

    @Mock private EleveRepository eleveRepository;
    @Mock private InscriptionRepository inscriptionRepository;
    @Mock private ClasseRepository classeRepository;

    private EleveService service;

    @BeforeEach
    void setUp() {
        service = new EleveService(eleveRepository, inscriptionRepository, classeRepository,
                new EleveMapper(inscriptionRepository, classeRepository), CLOCK);
    }

    @Test
    @DisplayName("La liste des élèves est triée et indique leur classe")
    void findAllAvecClasse() {
        when(eleveRepository.findAllByOrderByNomAscPrenomAsc()).thenReturn(List.of(eleve(1)));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(1, 1)));
        Classe classe = classe(1, "6A", 1);
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe));

        assertThat(service.findAll()).singleElement().satisfies(e -> {
            assertThat(e.nom()).isEqualTo("Kouassi");
            assertThat(e.nomClasse()).isEqualTo("6A");
        });
    }

    @Test
    @DisplayName("La création refuse une date de naissance dans le futur")
    void createRefuseDateFuture() {
        assertThatThrownBy(() -> service.create(new CreateEleveRequest(
                "Kouassi", "Aya", LocalDate.of(2030, 1, 1), 1)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("passé");
        verify(eleveRepository, never()).save(any());
    }

    @Test
    @DisplayName("La création refuse une classe pleine")
    void createRefuseClassePleine() {
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A", 1)));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(3L);

        assertThatThrownBy(() -> service.create(new CreateEleveRequest(
                "Kouassi", "Aya", LocalDate.of(2011, 2, 14), 1)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("effectif maximal");
    }

    @Test
    @DisplayName("La création enregistre la fiche et l'inscription courante")
    void createNominal() {
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A", 1)));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(1)).thenReturn(1L);
        when(eleveRepository.save(any(Eleve.class))).thenAnswer(invocation -> {
            Eleve e = invocation.getArgument(0);
            ReflectionTestUtils.setField(e, "idEleve", 9);
            return e;
        });
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(9)).thenReturn(Optional.empty());

        assertThat(service.create(new CreateEleveRequest(
                "Kouassi", "Aya", LocalDate.of(2011, 2, 14), 1)).idEleve()).isEqualTo(9);
        verify(inscriptionRepository).save(any(Inscription.class));
    }

    @Test
    @DisplayName("La suppression est refusée si l'élève a des notes")
    void deleteRefuseEleveAvecNotes() {
        Inscription inscription = inscription(1, 1);
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(inscriptionRepository.findAllByIdEleveOrderByDateInscriptionAsc(1))
                .thenReturn(List.of(inscription));
        when(inscriptionRepository.hasNotes(inscription.getNumeroInscription())).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("notes");
        verify(eleveRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Le changement de classe refuse un niveau différent")
    void changerClasseRefuseNiveauDifferent() {
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1))
                .thenReturn(Optional.of(inscription(1, 1)));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A", 1)));
        when(classeRepository.findById(3)).thenReturn(Optional.of(classe(3, "5A", 2)));

        assertThatThrownBy(() -> service.changerClasse(1, new ChangeClasseRequest(3)))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("même niveau");
    }

    @Test
    @DisplayName("Le changement de classe conserve l'ancienne inscription")
    void changerClasseNominal() {
        Inscription ancienne = inscription(1, 1);
        when(eleveRepository.findById(1)).thenReturn(Optional.of(eleve(1)));
        when(inscriptionRepository.findByIdEleveAndDateFinIsNull(1)).thenReturn(Optional.of(ancienne));
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe(1, "6A", 1)));
        when(classeRepository.findById(2)).thenReturn(Optional.of(classe(2, "6B", 1)));
        when(inscriptionRepository.countByIdClasseAndDateFinIsNull(2)).thenReturn(0L);
        when(inscriptionRepository.save(any(Inscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inscriptionRepository.findAllByIdEleveOrderByDateInscriptionAsc(1))
                .thenReturn(List.of(ancienne, inscription(2, 2)));

        var response = service.changerClasse(1, new ChangeClasseRequest(2));

        assertThat(response.ancienneClasse()).isEqualTo(1);
        assertThat(response.nouvelleClasse()).isEqualTo(2);
        assertThat(response.nombreInscriptions()).isEqualTo(2);
        assertThat(ancienne.getDateFin()).isEqualTo(LocalDate.of(2026, 11, 24));
    }

    /** Construit un élève avec son identifiant. */
    private Eleve eleve(Integer id) {
        Eleve e = new Eleve("Kouassi", "Aya", LocalDate.of(2011, 2, 14));
        ReflectionTestUtils.setField(e, "idEleve", id);
        return e;
    }

    /** Construit une classe avec son identifiant et son niveau. */
    private Classe classe(Integer id, String nom, Integer niveau) {
        Classe c = new Classe(nom, 2, niveau, 3);
        ReflectionTestUtils.setField(c, "idClasse", id);
        return c;
    }

    /** Construit une inscription courante. */
    private Inscription inscription(Integer id, Integer idClasse) {
        Inscription i = new Inscription(LocalDate.of(2026, 9, 2), 1, idClasse, "INS-2026-00" + id);
        ReflectionTestUtils.setField(i, "idInscription", id);
        return i;
    }
}

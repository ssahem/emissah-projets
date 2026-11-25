package com.suiviscolaire.suivi_scolaire_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suiviscolaire.suivi_scolaire_api.dto.request.AffectationMatiereRequest;
import com.suiviscolaire.suivi_scolaire_api.dto.request.CreateProfesseurRequest;
import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import com.suiviscolaire.suivi_scolaire_api.entity.Professeur;
import com.suiviscolaire.suivi_scolaire_api.exception.ConflitMetierException;
import com.suiviscolaire.suivi_scolaire_api.repository.ClasseRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.EnseignementRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.MatiereRepository;
import com.suiviscolaire.suivi_scolaire_api.repository.ProfesseurRepository;
import java.math.BigDecimal;
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
 * Règles métier F4 et F12 (professeurs et affectations).
 *
 * COUCHE       : Service
 * RÔLE         : Règles métier F4 et F12 (professeurs et affectations).
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ProfesseurServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ProfesseurRepository professeurRepository;
    @Mock private EnseignementRepository enseignementRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private ClasseRepository classeRepository;

    private ProfesseurService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new ProfesseurService(professeurRepository, enseignementRepository,
                matiereRepository, classeRepository);
    }

    // ── findAllAvecMatieres() ────────────────────────────────────────────────

    @Test
    @DisplayName("La liste des professeurs expose leurs matières issues de enseignement")
    void findAllAvecMatieres() {
        Professeur p = professeur(1);
        when(professeurRepository.findAllByOrderByNomAscPrenomAsc()).thenReturn(List.of(p));
        when(enseignementRepository.findDistinctMatiereByIdProfesseur(1)).thenReturn(List.of(1, 3));

        assertThat(service.findAll()).singleElement().satisfies(r -> {
            assertThat(r.nom()).isEqualTo("Durand");
            assertThat(r.matieres()).containsExactly(1, 3);
        });
    }

    // ── createRefuseEmailDuplique() ──────────────────────────────────────────

    @Test
    @DisplayName("La création refuse un email déjà utilisé")
    void createRefuseEmailDuplique() {
        when(professeurRepository.findByEmail("c.d@example.test"))
                .thenReturn(Optional.of(professeur(1)));

        assertThatThrownBy(() -> service.create(new CreateProfesseurRequest(
                "Durand", "Claire", "c.d@example.test", "0102", 1)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("déjà utilisé");
        verify(professeurRepository, never()).save(any());
    }

    // ── createVerifieMatiere() ───────────────────────────────────────────────

    @Test
    @DisplayName("La création vérifie que la matière principale existe")
    void createVerifieMatiere() {
        when(professeurRepository.findByEmail("nouveau@example.test")).thenReturn(Optional.empty());
        when(matiereRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateProfesseurRequest(
                "Petit", "Julie", "nouveau@example.test", "0102", 7)))
                .isInstanceOf(com.suiviscolaire.suivi_scolaire_api.exception
                        .RessourceIntrouvableException.class);
    }

    // ── deleteRefuseProfesseurAffecte() ──────────────────────────────────────

    @Test
    @DisplayName("La suppression est refusée si le professeur est affecté")
    void deleteRefuseProfesseurAffecte() {
        when(professeurRepository.findById(1)).thenReturn(Optional.of(professeur(1)));
        when(enseignementRepository.existsByIdProfesseur(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("affecté");
    }

    // ── affectationRefuseDoublon() ───────────────────────────────────────────

    @Test
    @DisplayName("Une affectation citant le titulaire est refusée avec un 409")
    void affectationRefuseDoublon() {
        Professeur titulaire = professeur(1);
        Enseignement existant = new Enseignement(1, 1, 1);
        when(professeurRepository.findById(2)).thenReturn(Optional.of(professeur(2)));
        when(classeRepository.findById(1))
                .thenReturn(Optional.of(new Classe("6A", 2, 1, 30)));
        when(matiereRepository.findById(1))
                .thenReturn(Optional.of(new Matiere("Mathematiques", "MATH", BigDecimal.ONE)));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1)).thenReturn(Optional.of(existant));
        when(professeurRepository.findById(1)).thenReturn(Optional.of(titulaire));

        assertThatThrownBy(() -> service.affecter(2, new AffectationMatiereRequest(1, 1)))
                .isInstanceOf(ConflitMetierException.class)
                .hasMessageContaining("Claire Durand");
    }

    // ── affectationNominale() ────────────────────────────────────────────────

    @Test
    @DisplayName("Une affectation libre est persistée")
    void affectationNominale() {
        when(professeurRepository.findById(2)).thenReturn(Optional.of(professeur(2)));
        Classe classe = new Classe("6A", 2, 1, 30);
        ReflectionTestUtils.setField(classe, "idClasse", 1);
        when(classeRepository.findById(1)).thenReturn(Optional.of(classe));
        Matiere matiere = new Matiere("Mathematiques", "MATH", BigDecimal.ONE);
        ReflectionTestUtils.setField(matiere, "idMatiere", 1);
        when(matiereRepository.findById(1)).thenReturn(Optional.of(matiere));
        when(enseignementRepository.findByIdClasseAndIdMatiere(1, 1)).thenReturn(Optional.empty());
        when(enseignementRepository.save(any(Enseignement.class))).thenAnswer(invocation -> {
            Enseignement e = invocation.getArgument(0);
            ReflectionTestUtils.setField(e, "idEnseignement", 10);
            return e;
        });

        assertThat(service.affecter(2, new AffectationMatiereRequest(1, 1)).idEnseignement())
                .isEqualTo(10);
    }


    // ── professeur() ─────────────────────────────────────────────────────────

    /**
     * Construit un professeur avec son identifiant.
     */

    private Professeur professeur(Integer id) {
        Professeur p = new Professeur("Durand", "Claire", "c.d@example.test", "0102030401");
        ReflectionTestUtils.setField(p, "idProfesseur", id);
        return p;
    }
}

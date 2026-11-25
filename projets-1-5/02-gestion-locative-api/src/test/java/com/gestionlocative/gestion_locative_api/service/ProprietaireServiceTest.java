package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.dto.request.CreateProprietaireRequest;
import com.gestionlocative.gestion_locative_api.dto.response.ImmeubleProprietaireResponse;
import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import com.gestionlocative.gestion_locative_api.entity.Proprietaire;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireEmailDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNonSupprimableException;
import com.gestionlocative.gestion_locative_api.exception.ProprietaireNotFoundException;
import com.gestionlocative.gestion_locative_api.mapper.ImmeubleMapper;
import com.gestionlocative.gestion_locative_api.mapper.ProprietaireMapper;
import com.gestionlocative.gestion_locative_api.repository.AppartementRepository;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.ImmeubleRepository;
import com.gestionlocative.gestion_locative_api.repository.ProprietaireRepository;
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
 * Tests unitaires du service des propriétaires (F1).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des propriétaires (F1).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mapper réel.
 * CAS COUVERTS : email en doublon, suppression avec immeubles, identifiant inconnu.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class ProprietaireServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private ProprietaireRepository proprietaireRepository;
    @Mock private ImmeubleRepository immeubleRepository;
    @Mock private AppartementRepository appartementRepository;
    @Mock private BailRepository bailRepository;

    private ProprietaireService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new ProprietaireService(proprietaireRepository, immeubleRepository,
                appartementRepository, bailRepository, new ProprietaireMapper(),
                new ImmeubleMapper());
    }

    // ── create_throwsWhenEmailDejaUtilise() ──────────────────────────────────

    @Test
    @DisplayName("La création refuse un email déjà utilisé")
    void create_throwsWhenEmailDejaUtilise() {
        // ── Préparation ──────────────────────────────────────────────────────
        Proprietaire existant = new Proprietaire("Diallo", "Awa", "awa@example.test", "0102");
        ReflectionTestUtils.setField(existant, "idProprietaire", 1);
        when(proprietaireRepository.findByEmail("awa@example.test"))
                .thenReturn(Optional.of(existant));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(
                new CreateProprietaireRequest("Konate", "Aminata", "awa@example.test", "0103")))
                .isInstanceOf(ProprietaireEmailDejaUtiliseException.class);
    }

    // ── delete_throwsWhenImmeublesExist() ────────────────────────────────────

    @Test
    @DisplayName("La suppression refuse un propriétaire qui possède des immeubles")
    void delete_throwsWhenImmeublesExist() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(proprietaireRepository.findById(1)).thenReturn(Optional.of(
                new Proprietaire("Diallo", "Awa", "awa@example.test", "0102")));
        when(immeubleRepository.countByIdProprietaire(1)).thenReturn(2L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(ProprietaireNonSupprimableException.class);
    }

    // ── findById_throwsWhenMissing() ─────────────────────────────────────────

    @Test
    @DisplayName("Un identifiant inconnu renvoie l'erreur métier 404")
    void findById_throwsWhenMissing() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(proprietaireRepository.findById(99)).thenReturn(Optional.empty());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(ProprietaireNotFoundException.class);
    }

    // ── findImmeubles_renvoieCompteurs() ─────────────────────────────────────

    @Test
    @DisplayName("F7 — les immeubles d'un propriétaire portent leurs compteurs")
    void findImmeubles_renvoieCompteurs() {
        // ── Préparation ──────────────────────────────────────────────────────
        Proprietaire proprietaire = new Proprietaire("Diallo", "Awa", "awa@example.test", "0102");
        ReflectionTestUtils.setField(proprietaire, "idProprietaire", 1);
        Immeuble immeuble = new Immeuble("12 rue des Acacias", "75001", "Paris", 1, 3);
        ReflectionTestUtils.setField(immeuble, "idImmeuble", 2);
        when(proprietaireRepository.findById(1)).thenReturn(Optional.of(proprietaire));
        when(immeubleRepository.findAllByIdProprietaireOrderByAdresseAsc(1))
                .thenReturn(List.of(immeuble));
        when(appartementRepository.countByIdImmeuble(2)).thenReturn(6L);
        when(bailRepository.countEnCoursByIdImmeuble(2, StatutBail.EN_COURS)).thenReturn(4L);

        // ── Action ───────────────────────────────────────────────────────────
        List<ImmeubleProprietaireResponse> resultat = service.findImmeubles(1);

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat).hasSize(1);
        assertThat(resultat.getFirst().nbAppartements()).isEqualTo(6L);
        assertThat(resultat.getFirst().nbBauxEnCours()).isEqualTo(4L);
    }
}

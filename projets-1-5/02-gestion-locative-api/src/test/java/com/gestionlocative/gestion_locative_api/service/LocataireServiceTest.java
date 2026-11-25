package com.gestionlocative.gestion_locative_api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gestionlocative.gestion_locative_api.dto.request.UpdateLocataireRequest;
import com.gestionlocative.gestion_locative_api.entity.Locataire;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import com.gestionlocative.gestion_locative_api.exception.LocataireEmailDejaUtiliseException;
import com.gestionlocative.gestion_locative_api.exception.LocataireNonSupprimableException;
import com.gestionlocative.gestion_locative_api.mapper.LocataireMapper;
import com.gestionlocative.gestion_locative_api.repository.BailRepository;
import com.gestionlocative.gestion_locative_api.repository.LocataireRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires du service des locataires (F4).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des locataires (F4).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mapper réel.
 * CAS COUVERTS : email en doublon après modification, bail en cours, identifiant inconnu.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class LocataireServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private LocataireRepository locataireRepository;
    @Mock private BailRepository bailRepository;

    private LocataireService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new LocataireService(locataireRepository, bailRepository, new LocataireMapper());
    }

    // ── update_throwsWhenEmailDejaUtilise() ──────────────────────────────────

    @Test
    @DisplayName("La modification refuse un email déjà utilisé par un autre locataire")
    void update_throwsWhenEmailDejaUtilise() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(locataireRepository.findById(1)).thenReturn(Optional.of(
                new Locataire("Traore", "Kofi", "kofi@example.test", "0102")));
        Locataire existant = new Locataire("Diallo", "Awa", "autre@example.test", "0103");
        ReflectionTestUtils.setField(existant, "idLocataire", 2);
        when(locataireRepository.findByEmail("autre@example.test"))
                .thenReturn(Optional.of(existant));

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(1,
                new UpdateLocataireRequest("Traore", "Kofi", "autre@example.test", "0102")))
                .isInstanceOf(LocataireEmailDejaUtiliseException.class);
    }

    // ── delete_throwsWhenBailEnCours() ───────────────────────────────────────

    @Test
    @DisplayName("La suppression refuse un locataire titulaire d'un bail en cours")
    void delete_throwsWhenBailEnCours() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(locataireRepository.findById(1)).thenReturn(Optional.of(
                new Locataire("Traore", "Kofi", "kofi@example.test", "0102")));
        when(bailRepository.countEnCoursByLocataire(1, StatutBail.EN_COURS)).thenReturn(1L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(LocataireNonSupprimableException.class);
    }
}

package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.dto.request.CreateSalleRequest;
import com.billetterie.billetterie_api.dto.request.UpdateSalleRequest;
import com.billetterie.billetterie_api.exception.CapaciteInsuffisanteException;
import com.billetterie.billetterie_api.exception.CategorieInconnueException;
import com.billetterie.billetterie_api.exception.SalleDejaExistanteException;
import com.billetterie.billetterie_api.exception.SalleNonSupprimableException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.mapper.EvenementMapper;
import com.billetterie.billetterie_api.mapper.PlaceMapper;
import com.billetterie.billetterie_api.mapper.SalleMapper;
import com.billetterie.billetterie_api.repository.CategorieRepository;
import com.billetterie.billetterie_api.repository.EvenementRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service des salles.
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du service des salles.
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mappers réels.
 * CAS COUVERTS : doublon, capacité, suppression, catégorie, identifiant inconnu.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class SalleServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private SalleRepository salleRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private EvenementRepository evenementRepository;
    @Mock private CategorieRepository categorieRepository;

    private SalleService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Initialise le service et les collaborateurs simulés nécessaires aux
     * tests.
     */

    @BeforeEach
    void setUp() {
        service = new SalleService(salleRepository, placeRepository, evenementRepository,
                categorieRepository, new SalleMapper(), new PlaceMapper(),
                new EvenementMapper(Clock.systemUTC()));
    }

    // ── create_throwsWhenDuplicate() ─────────────────────────────────────────

    @Test
    @DisplayName("La création refuse un couple nom/ville déjà utilisé")
    void create_throwsWhenDuplicate() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.existsByNomIgnoreCaseAndVilleIgnoreCase("Grand Hall", "Abidjan"))
                .thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(
                new CreateSalleRequest("Grand Hall", "12 rue des Arts", "Abidjan", 120)))
                .isInstanceOf(SalleDejaExistanteException.class);
    }

    // ── update_throwsWhenCapacityTooLow() ────────────────────────────────────

    @Test
    @DisplayName("La modification refuse une capacité inférieure aux places existantes")
    void update_throwsWhenCapacityTooLow() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(
                new com.billetterie.billetterie_api.entity.Salle(
                        "Grand Hall", "12 rue des Arts", "Abidjan", 120)));
        when(placeRepository.countBySalle_IdSalle(1)).thenReturn(50L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(1,
                new UpdateSalleRequest("Grand Hall", "12 rue des Arts", "Abidjan", 10)))
                .isInstanceOf(CapaciteInsuffisanteException.class);
    }

    // ── delete_throwsWhenPlacesExist() ───────────────────────────────────────

    @Test
    @DisplayName("La suppression refuse une salle qui possède des places")
    void delete_throwsWhenPlacesExist() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(
                new com.billetterie.billetterie_api.entity.Salle(
                        "Grand Hall", "12 rue des Arts", "Abidjan", 120)));
        when(salleRepository.countPlacesByIdSalle(1)).thenReturn(1L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(SalleNonSupprimableException.class);
    }

    // ── findById_throwsWhenMissing() ─────────────────────────────────────────

    @Test
    @DisplayName("Une salle inconnue renvoie l'erreur métier 404")
    void findById_throwsWhenMissing() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(99)).thenReturn(Optional.empty());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(SalleNotFoundException.class);
    }

    // ── findPlaces_throwsWhenCategorieUnknown() ──────────────────────────────

    @Test
    @DisplayName("Une catégorie inconnue est refusée pour le filtre des places")
    void findPlaces_throwsWhenCategorieUnknown() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(
                new com.billetterie.billetterie_api.entity.Salle(
                        "Grand Hall", "12 rue des Arts", "Abidjan", 120)));
        when(categorieRepository.findByLibelleIgnoreCase("INCONNUE")).thenReturn(Optional.empty());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.findPlaces(1, "INCONNUE"))
                .isInstanceOf(CategorieInconnueException.class);
    }
}

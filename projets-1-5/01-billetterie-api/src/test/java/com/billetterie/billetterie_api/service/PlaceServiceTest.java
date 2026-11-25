package com.billetterie.billetterie_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.billetterie.billetterie_api.dto.request.CreatePlaceRequest;
import com.billetterie.billetterie_api.dto.request.UpdatePlaceRequest;
import com.billetterie.billetterie_api.dto.response.PlaceResponse;
import com.billetterie.billetterie_api.entity.Categorie;
import com.billetterie.billetterie_api.entity.Place;
import com.billetterie.billetterie_api.entity.Salle;
import com.billetterie.billetterie_api.exception.CapaciteInsuffisanteException;
import com.billetterie.billetterie_api.exception.CategorieInconnueException;
import com.billetterie.billetterie_api.exception.PlaceDejaExistanteException;
import com.billetterie.billetterie_api.exception.PlaceNonSupprimableException;
import com.billetterie.billetterie_api.exception.PlacePositionFigeeException;
import com.billetterie.billetterie_api.exception.SalleNotFoundException;
import com.billetterie.billetterie_api.mapper.PlaceMapper;
import com.billetterie.billetterie_api.repository.CategorieRepository;
import com.billetterie.billetterie_api.repository.PlaceRepository;
import com.billetterie.billetterie_api.repository.ReservationPlaceRepository;
import com.billetterie.billetterie_api.repository.SalleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires du CRUD des places (F2).
 *
 * COUCHE       : Service
 * RÔLE         : Tests unitaires du CRUD des places (F2).
 * TYPE         : Unitaire (Mockito)
 * STRATÉGIE    : repositories simulés, mapper réel.
 * CAS COUVERTS : doublon, capacité, salle/catégorie inconnue, position figée, suppression refusée.
 * CONVENTION   : Préparation → Action → Vérification.
 * NE FAIT PAS  : d’accès HTTP ni de validation de format.
 */
@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Mock private PlaceRepository placeRepository;
    @Mock private SalleRepository salleRepository;
    @Mock private CategorieRepository categorieRepository;
    @Mock private ReservationPlaceRepository reservationPlaceRepository;

    private PlaceService service;

    // ── setUp() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour setUp().
     */

    @BeforeEach
    void setUp() {
        service = new PlaceService(placeRepository, salleRepository, categorieRepository,
                reservationPlaceRepository, new PlaceMapper());
    }

    // ── salle() ──────────────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour salle().
     */

    private Salle salle(int capacite) {
        Salle salle = new Salle("Grand Hall", "12 rue des Arts", "Abidjan", capacite);
        ReflectionTestUtils.setField(salle, "idSalle", 1);
        return salle;
    }

    // ── createRequest() ──────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour createRequest().
     */

    private CreatePlaceRequest createRequest() {
        return new CreatePlaceRequest(1, "A", 13, "ORCHESTRE");
    }

    // ── updateRequest() ──────────────────────────────────────────────────────

    /**
     * Prépare la donnée de test pour updateRequest().
     */

    private UpdatePlaceRequest updateRequest() {
        return new UpdatePlaceRequest(1, "A", 13, "ORCHESTRE");
    }

    // ── create_throwsWhenDoublon() ───────────────────────────────────────────

    @Test
    @DisplayName("La création refuse une position déjà utilisée dans la salle")
    void create_throwsWhenDoublon() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle(120)));
        when(categorieRepository.findByLibelleIgnoreCase("ORCHESTRE"))
                .thenReturn(Optional.of(new Categorie("ORCHESTRE")));
        when(placeRepository.existsBySalle_IdSalleAndRangAndNumero(1, "A", 13)).thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(createRequest()))
                .isInstanceOf(PlaceDejaExistanteException.class);
    }

    // ── create_throwsWhenCapaciteAtteinte() ──────────────────────────────────

    @Test
    @DisplayName("La création refuse de dépasser la capacité de la salle")
    void create_throwsWhenCapaciteAtteinte() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle(1)));
        when(categorieRepository.findByLibelleIgnoreCase("ORCHESTRE"))
                .thenReturn(Optional.of(new Categorie("ORCHESTRE")));
        when(placeRepository.countBySalle_IdSalle(1)).thenReturn(1L);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(createRequest()))
                .isInstanceOf(CapaciteInsuffisanteException.class);
    }

    // ── create_throwsWhenSalleInconnue() ─────────────────────────────────────

    @Test
    @DisplayName("La création refuse une salle inconnue")
    void create_throwsWhenSalleInconnue() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.empty());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(createRequest()))
                .isInstanceOf(SalleNotFoundException.class);
    }

    // ── create_throwsWhenCategorieInconnue() ─────────────────────────────────

    @Test
    @DisplayName("La création refuse une catégorie inconnue")
    void create_throwsWhenCategorieInconnue() {
        // ── Préparation ──────────────────────────────────────────────────────
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle(120)));
        when(categorieRepository.findByLibelleIgnoreCase("ORCHESTRE")).thenReturn(Optional.empty());

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.create(createRequest()))
                .isInstanceOf(CategorieInconnueException.class);
    }

    // ── create_returnsPlace() ────────────────────────────────────────────────

    @Test
    @DisplayName("La création renvoie la place enregistrée")
    void create_returnsPlace() {
        // ── Préparation ──────────────────────────────────────────────────────
        Salle salle = salle(120);
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle));
        when(categorieRepository.findByLibelleIgnoreCase("ORCHESTRE"))
                .thenReturn(Optional.of(new Categorie("ORCHESTRE")));
        when(placeRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Place.class)))
                .thenAnswer(invocation -> {
                    Place place = invocation.getArgument(0);
                    ReflectionTestUtils.setField(place, "idPlace", 5);
                    return place;
                });

        // ── Action ───────────────────────────────────────────────────────────
        PlaceResponse resultat = service.create(createRequest());

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.idPlace()).isEqualTo(5);
        assertThat(resultat.idSalle()).isEqualTo(1);
        assertThat(resultat.rang()).isEqualTo("A");
        assertThat(resultat.numero()).isEqualTo(13);
        assertThat(resultat.categorie()).isEqualTo("ORCHESTRE");
    }

    // ── update_throwsWhenPlaceReservee() ─────────────────────────────────────

    @Test
    @DisplayName("La position d'une place réservée est figée")
    void update_throwsWhenPlaceReservee() {
        // ── Préparation ──────────────────────────────────────────────────────
        Place place = new Place("A", 12, salle(120), new Categorie("ORCHESTRE"));
        when(placeRepository.findById(1)).thenReturn(Optional.of(place));
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle(120)));
        when(categorieRepository.findByLibelleIgnoreCase("ORCHESTRE"))
                .thenReturn(Optional.of(new Categorie("ORCHESTRE")));
        when(reservationPlaceRepository.existsByRangAndNumero("A", 12)).thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.update(1, updateRequest()))
                .isInstanceOf(PlacePositionFigeeException.class);
    }

    // ── update_acceptsCategorieSeule() ───────────────────────────────────────

    @Test
    @DisplayName("La catégorie d'une place réservée reste modifiable")
    void update_acceptsCategorieSeule() {
        // ── Préparation ──────────────────────────────────────────────────────
        Place place = new Place("A", 12, salle(120), new Categorie("ORCHESTRE"));
        when(placeRepository.findById(1)).thenReturn(Optional.of(place));
        when(salleRepository.findById(1)).thenReturn(Optional.of(salle(120)));
        when(categorieRepository.findByLibelleIgnoreCase("BALCON"))
                .thenReturn(Optional.of(new Categorie("BALCON")));

        // ── Action ───────────────────────────────────────────────────────────
        PlaceResponse resultat = service.update(1,
                new UpdatePlaceRequest(1, "A", 12, "BALCON"));

        // ── Vérification ─────────────────────────────────────────────────────
        assertThat(resultat.categorie()).isEqualTo("BALCON");
    }

    // ── delete_throwsWhenPlaceReservee() ─────────────────────────────────────

    @Test
    @DisplayName("La suppression d'une place réservée est refusée")
    void delete_throwsWhenPlaceReservee() {
        // ── Préparation ──────────────────────────────────────────────────────
        Place place = new Place("A", 12, salle(120), new Categorie("ORCHESTRE"));
        when(placeRepository.findById(1)).thenReturn(Optional.of(place));
        when(reservationPlaceRepository.existsByRangAndNumero("A", 12)).thenReturn(true);

        // ── Action + Vérification ────────────────────────────────────────────
        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(PlaceNonSupprimableException.class);
    }
}

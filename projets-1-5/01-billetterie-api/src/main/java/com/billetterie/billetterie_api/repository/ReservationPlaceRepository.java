package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.ReservationPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux places réservées.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des ReservationPlace ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ReservationPlaceRepository extends JpaRepository<ReservationPlace, Integer> {

    List<ReservationPlace> findAllByReservation_Evenement_IdEvenementAndRangAndNumero(
            Integer idEvenement, String rang, Integer numero);

    boolean existsByReservation_Evenement_IdEvenementAndRangAndNumero(
            Integer idEvenement, String rang, Integer numero);

    boolean existsByRangAndNumeroAndReservation_Evenement_Salle_IdSalle(
            String rang, Integer numero, Integer idSalle);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── existsByRangAndNumero() ──────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByRangAndNumero(String rang, Integer numero);

    // ── findAllByReservation_Evenement_IdEvenement() ─────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<ReservationPlace> findAllByReservation_Evenement_IdEvenement(Integer idEvenement);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @Query
    // ═════════════════════════════════════════════════════════════════════════

    /** Places occupées par une réservation encore active, déduites de la vue statut. */
    @Query(value = """
            SELECT rp.* FROM reservation_place rp
            JOIN v_reservation_statut v ON v.id_reservation = rp.id_reservation
            WHERE v.id_evenement = :idEvenement AND v.statut IN ('PROVISOIRE', 'PAYEE')
            """, nativeQuery = true)

    // ── findPlacesActivesByEvenement() ───────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<ReservationPlace> findPlacesActivesByEvenement(@Param("idEvenement") Integer idEvenement);

    /** Places occupées par un statut donné, déduites de la vue statut (F18). */
    @Query(value = """
            SELECT rp.* FROM reservation_place rp
            JOIN v_reservation_statut v ON v.id_reservation = rp.id_reservation
            WHERE v.id_evenement = :idEvenement AND v.statut = :statut
            """, nativeQuery = true)
    List<ReservationPlace> findPlacesByEvenementAndStatut(
            @Param("idEvenement") Integer idEvenement, @Param("statut") String statut);
}

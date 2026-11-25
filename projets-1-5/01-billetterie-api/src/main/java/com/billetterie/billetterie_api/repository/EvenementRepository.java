package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Evenement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux événements.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Evenement ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface EvenementRepository extends JpaRepository<Evenement, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByDateHeureAsc() ───────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Evenement> findAllByOrderByDateHeureAsc();

    // ── findAllByDateHeureGreaterThanEqualOrderByDateHeureAsc() ──────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Evenement> findAllByDateHeureGreaterThanEqualOrderByDateHeureAsc(LocalDateTime du);

    // ── findAllByDateHeureLessThanEqualOrderByDateHeureAsc() ─────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Evenement> findAllByDateHeureLessThanEqualOrderByDateHeureAsc(LocalDateTime au);

    // ── findAllByDateHeureBetweenOrderByDateHeureAsc() ───────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Evenement> findAllByDateHeureBetweenOrderByDateHeureAsc(LocalDateTime du, LocalDateTime au);

    // ── findAllBySalle_IdSalleOrderByDateHeureAsc() ──────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Evenement> findAllBySalle_IdSalleOrderByDateHeureAsc(Integer idSalle);

    // ── existsBySalle_IdSalle() ──────────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsBySalle_IdSalle(Integer idSalle);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @QUERY
    // ═════════════════════════════════════════════════════════════════════════

    // ── countReservationsByIdEvenement() ─────────────────────────────────────

    /** Compte les réservations d'un événement. */
    @Query("select count(r) from Reservation r where r.evenement.idEvenement = :idEvenement")
    long countReservationsByIdEvenement(@Param("idEvenement") Integer idEvenement);

    // ── countPlacesByIdSalle() ───────────────────────────────────────────────

    /** Compte les places d'une salle. */
    @Query("select count(p) from Place p where p.salle.idSalle = :idSalle")
    long countPlacesByIdSalle(@Param("idSalle") Integer idSalle);
}

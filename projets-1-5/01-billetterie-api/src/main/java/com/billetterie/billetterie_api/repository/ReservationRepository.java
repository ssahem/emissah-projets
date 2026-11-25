package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux réservations.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Reservation ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByDateCreationDesc() ───────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Reservation> findAllByOrderByDateCreationDesc();

    // ── findAllByClient_EmailIgnoreCaseOrderByDateCreationDesc() ─────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Reservation> findAllByClient_EmailIgnoreCaseOrderByDateCreationDesc(String email);

    // ── findAllByEvenement_IdEvenementOrderByDateCreationDesc() ──────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Reservation> findAllByEvenement_IdEvenementOrderByDateCreationDesc(Integer idEvenement);

    // ── existsByEvenement_IdEvenement() ──────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByEvenement_IdEvenement(Integer idEvenement);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @QUERY
    // ═════════════════════════════════════════════════════════════════════════

    // ── findProvisoiresExpirees() ────────────────────────────────────────────

    /** Réservations PROVISOIRE créées avant la limite, pour la tâche d'expiration (F16). */
    @Query(value = """
            SELECT r.* FROM reservation r
            JOIN v_reservation_statut v ON v.id_reservation = r.id_reservation
            WHERE v.statut = 'PROVISOIRE' AND r.date_creation < :limite
            """, nativeQuery = true)
    List<Reservation> findProvisoiresExpirees(@Param("limite") LocalDateTime limite);

    // ── findLignesExport() ───────────────────────────────────────────────────

    /** Lignes d'export CSV : rang, numero, categorie, nom, prenom, email, statut, date_creation. */
    @Query(value = """
            SELECT rp.rang, rp.numero, cat.libelle, c.nom, c.prenom, c.email,
                   v.statut, r.date_creation
            FROM reservation r
            JOIN client c ON c.id_client = r.id_client
            JOIN v_reservation_statut v ON v.id_reservation = r.id_reservation
            JOIN evenement e ON e.id_evenement = r.id_evenement
            LEFT JOIN reservation_place rp ON rp.id_reservation = r.id_reservation
            LEFT JOIN place p ON p.id_salle = e.id_salle
                            AND p.rang = rp.rang AND p.numero = rp.numero
            LEFT JOIN categorie cat ON cat.id_categorie = p.id_categorie
            WHERE r.id_evenement = :idEvenement
              AND (:inclureAnnulees = true OR v.statut <> 'ANNULEE')
            ORDER BY r.date_creation DESC
            """, nativeQuery = true)
    List<Object[]> findLignesExport(@Param("idEvenement") Integer idEvenement,
                                    @Param("inclureAnnulees") boolean inclureAnnulees);
}

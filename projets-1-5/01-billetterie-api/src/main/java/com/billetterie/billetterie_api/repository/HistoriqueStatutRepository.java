package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.HistoriqueStatut;
import com.billetterie.billetterie_api.entity.HistoriqueStatutId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès à l'historique des statuts.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des HistoriqueStatut ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, HistoriqueStatutId> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════
    // ── findTopByReservation_IdReservationOrderByDateStatutDesc() ────────

    /** Requête Spring Data déduite du nom de la méthode. */
    Optional<HistoriqueStatut> findTopByReservation_IdReservationOrderByDateStatutDesc(
            Integer idReservation);

    // ── existsByReservation_IdReservationAndStatut_LibelleIgnoreCase() ───

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByReservation_IdReservationAndStatut_LibelleIgnoreCase(
            Integer idReservation, String libelle);
}

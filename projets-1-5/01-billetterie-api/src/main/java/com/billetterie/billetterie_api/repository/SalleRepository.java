package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Salle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux salles.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Salle ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface SalleRepository extends JpaRepository<Salle, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByNomAscVilleAsc() ─────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Salle> findAllByOrderByNomAscVilleAsc();

    // ── existsByNomIgnoreCaseAndVilleIgnoreCase() ────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByNomIgnoreCaseAndVilleIgnoreCase(String nom, String ville);

    // ── existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdSalleNot() ────────────

    /** Vérifie si un autre salon porte déjà le même nom et la même ville. */
    boolean existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdSalleNot(
            String nom, String ville, Integer idSalle);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @Query
    // ═════════════════════════════════════════════════════════════════════════

    // ── countPlacesByIdSalle() ───────────────────────────────────────────

    /** Compte les places d'une salle. */
    @Query("select count(p) from Place p where p.salle.idSalle = :idSalle")
    long countPlacesByIdSalle(@Param("idSalle") Integer idSalle);

    // ── countEvenementsByIdSalle() ───────────────────────────────────────

    /** Compte les événements prévus dans une salle. */
    @Query("select count(e) from Evenement e where e.salle.idSalle = :idSalle")
    long countEvenementsByIdSalle(@Param("idSalle") Integer idSalle);
}

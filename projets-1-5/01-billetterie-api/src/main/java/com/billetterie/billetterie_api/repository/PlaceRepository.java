package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux places.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Place ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface PlaceRepository extends JpaRepository<Place, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByRangAscNumeroAsc() ───────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Place> findAllByOrderByRangAscNumeroAsc();

    // ── findAllBySalle_IdSalleOrderByRangAscNumeroAsc() ──────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    List<Place> findAllBySalle_IdSalleOrderByRangAscNumeroAsc(Integer idSalle);

    // ── findAllBySalle_IdSalleAndCategorie_Libelle…() ────────────────────────

    /** Liste les places d'une salle d'une catégorie, dans l'ordre du plan. */
    List<Place> findAllBySalle_IdSalleAndCategorie_LibelleIgnoreCaseOrderByRangAscNumeroAsc(
            Integer idSalle, String libelle);

    // ── findBySalle_IdSalleAndRangAndNumero() ────────────────────────────────

    /**
     * Requête Spring Data déduite du nom de la méthode.
     * @return l'élément s'il existe, sinon {@link Optional#empty()} (jamais null)
     */
    Optional<Place> findBySalle_IdSalleAndRangAndNumero(Integer idSalle, String rang, Integer numero);

    // ── findFirstByRangAndNumero() ───────────────────────────────────────────

    /**
     * Requête Spring Data déduite du nom de la méthode.
     * @return l'élément s'il existe, sinon {@link Optional#empty()} (jamais null)
     */
    Optional<Place> findFirstByRangAndNumero(String rang, Integer numero);

    // ── existsBySalle_IdSalleAndRangAndNumero() ──────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsBySalle_IdSalleAndRangAndNumero(Integer idSalle, String rang, Integer numero);

    // ── countBySalle_IdSalle() ───────────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    long countBySalle_IdSalle(Integer idSalle);
}

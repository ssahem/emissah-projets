package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Categorie;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Accès aux catégories.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des Categorie (table de référence) ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface CategorieRepository extends Repository<Categorie, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByLibelleIgnoreCase() ────────────────────────────────────────────

    /**
     * Requête Spring Data déduite du nom de la méthode.
     * @return l'élément s'il existe, sinon {@link Optional#empty()} (jamais null)
     */
    Optional<Categorie> findByLibelleIgnoreCase(String libelle);

    // ── existsByLibelleIgnoreCase() ──────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByLibelleIgnoreCase(String libelle);
}

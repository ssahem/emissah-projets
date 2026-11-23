package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Categorie;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux catégories.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Categorie ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByLibelleIgnoreCase() ────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    Optional<Categorie> findByLibelleIgnoreCase(String libelle);
    // ── existsByLibelleIgnoreCase() ──────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByLibelleIgnoreCase(String libelle);
}

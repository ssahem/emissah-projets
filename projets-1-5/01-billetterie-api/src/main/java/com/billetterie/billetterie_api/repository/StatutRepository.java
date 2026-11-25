package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Statut;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Accès aux statuts.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des Statut (table de référence) ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface StatutRepository extends Repository<Statut, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByLibelleIgnoreCase() ────────────────────────────────────────────

    /**
     * Requête Spring Data déduite du nom de la méthode.
     * @return l'élément s'il existe, sinon {@link Optional#empty()} (jamais null)
     */
    Optional<Statut> findByLibelleIgnoreCase(String libelle);
}

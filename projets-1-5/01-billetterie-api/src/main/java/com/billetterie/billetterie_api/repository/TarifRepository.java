package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Tarif;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Accès aux tarifs.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des Tarif (table de référence) ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface TarifRepository extends Repository<Tarif, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByEvenement_IdEvenementAndCategorieIgnoreCase() ──────────────

    /**
     * Requête Spring Data déduite du nom de la méthode.
     * @return l'élément s'il existe, sinon {@link Optional#empty()} (jamais null)
     */
    Optional<Tarif> findByEvenement_IdEvenementAndCategorieIgnoreCase(
            Integer idEvenement, String categorie);
}

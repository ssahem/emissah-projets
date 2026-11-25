package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Depense;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux dépenses.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link Depense}.
 * UTILISÉ PAR  : les vues SQL de dépenses.
 * NE FAIT PAS  : de calcul de coût pour F18 (D-03).
 */
public interface DepenseRepository extends JpaRepository<Depense, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

}

package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.TypeDepense;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux catégories de dépense.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link TypeDepense}.
 * UTILISÉ PAR  : les vues SQL de dépenses.
 * NE FAIT PAS  : de règle métier.
 */
public interface TypeDepenseRepository extends JpaRepository<TypeDepense, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

}

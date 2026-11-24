package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Référentiel des types d'intervention, résolus par code (A-04).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à TypeIntervention.
 * DÉPEND DE    : Spring Data JPA et l'entité TypeIntervention.
 * UTILISÉ PAR  : TypeInterventionService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface TypeInterventionRepository extends JpaRepository<TypeIntervention, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    Optional<TypeIntervention> findByCode(String code);
}

package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.TypeIntervention;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Référentiel des types d'intervention, résolus par code (A-04). */
public interface TypeInterventionRepository extends JpaRepository<TypeIntervention, Integer> {

    Optional<TypeIntervention> findByCode(String code);
}

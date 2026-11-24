package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Référentiel des statuts (V7). */
public interface StatutRepository extends JpaRepository<Statut, Integer> {

    Optional<Statut> findByLibelle(String libelle);
}

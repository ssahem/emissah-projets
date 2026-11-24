package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux agences (F1). */
public interface AgenceRepository extends JpaRepository<Agence, Integer> {

    List<Agence> findAllByOrderByVilleAscNomAsc();

    Optional<Agence> findByNom(String nom);
}

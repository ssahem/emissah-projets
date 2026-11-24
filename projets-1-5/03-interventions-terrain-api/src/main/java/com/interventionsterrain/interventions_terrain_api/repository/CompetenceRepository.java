package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès au référentiel de compétences (F3). */
public interface CompetenceRepository extends JpaRepository<Competence, Integer> {

    List<Competence> findAllByOrderByLibelleAsc();

    Optional<Competence> findByLibelle(String libelle);

    boolean existsByLibelle(String libelle);
}

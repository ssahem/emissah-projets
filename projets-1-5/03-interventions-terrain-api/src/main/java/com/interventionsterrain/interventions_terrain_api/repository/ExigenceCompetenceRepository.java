package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetenceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Compétences exigées par type (F3, F13). */
public interface ExigenceCompetenceRepository extends JpaRepository<ExigenceCompetence, ExigenceCompetenceId> {

    boolean existsByIdIdCompetence(Integer idCompetence);

    List<ExigenceCompetence> findAllByIdIdType(Integer idType);
}

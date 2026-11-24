package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux équipes : seul chemin entre technicien et agence (A-01). */
public interface EquipeRepository extends JpaRepository<Equipe, Integer> {

    List<Equipe> findAllByIdAgence(Integer idAgence);

    Optional<Equipe> findFirstByIdAgenceOrderByIdEquipeAsc(Integer idAgence);

    boolean existsByIdAgence(Integer idAgence);

    boolean existsByIdEquipeAndIdAgence(Integer idEquipe, Integer idAgence);
}

package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetence;
import com.interventionsterrain.interventions_terrain_api.entity.ExigenceCompetenceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Compétences exigées par type (F3, F13).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à ExigenceCompetence.
 * DÉPEND DE    : Spring Data JPA et l'entité ExigenceCompetence.
 * UTILISÉ PAR  : ExigenceCompetenceService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface ExigenceCompetenceRepository extends JpaRepository<ExigenceCompetence, ExigenceCompetenceId> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    boolean existsByIdIdCompetence(Integer idCompetence);

    List<ExigenceCompetence> findAllByIdIdType(Integer idType);
}

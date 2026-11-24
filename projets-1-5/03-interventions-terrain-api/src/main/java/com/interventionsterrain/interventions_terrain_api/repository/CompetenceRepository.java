package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Competence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès au référentiel de compétences (F3).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Competence.
 * DÉPEND DE    : Spring Data JPA et l'entité Competence.
 * UTILISÉ PAR  : CompetenceService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface CompetenceRepository extends JpaRepository<Competence, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    List<Competence> findAllByOrderByLibelleAsc();

    Optional<Competence> findByLibelle(String libelle);

    boolean existsByLibelle(String libelle);
}

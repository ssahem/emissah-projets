package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Equipe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux équipes : seul chemin entre technicien et agence (A-01).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Equipe.
 * DÉPEND DE    : Spring Data JPA et l'entité Equipe.
 * UTILISÉ PAR  : EquipeService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface EquipeRepository extends JpaRepository<Equipe, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    List<Equipe> findAllByIdAgence(Integer idAgence);

    Optional<Equipe> findFirstByIdAgenceOrderByIdEquipeAsc(Integer idAgence);

    boolean existsByIdAgence(Integer idAgence);

    boolean existsByIdEquipeAndIdAgence(Integer idEquipe, Integer idAgence);
}

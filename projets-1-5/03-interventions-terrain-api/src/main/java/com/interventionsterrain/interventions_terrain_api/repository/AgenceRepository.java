package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Agence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux agences (F1).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Agence.
 * DÉPEND DE    : Spring Data JPA et l'entité Agence.
 * UTILISÉ PAR  : AgenceService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface AgenceRepository extends JpaRepository<Agence, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    List<Agence> findAllByOrderByVilleAscNomAsc();

    Optional<Agence> findByNom(String nom);
}

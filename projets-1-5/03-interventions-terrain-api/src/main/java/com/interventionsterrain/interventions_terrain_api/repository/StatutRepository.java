package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Statut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Référentiel des statuts (V7).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Statut.
 * DÉPEND DE    : Spring Data JPA et l'entité Statut.
 * UTILISÉ PAR  : StatutService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface StatutRepository extends JpaRepository<Statut, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    Optional<Statut> findByLibelle(String libelle);
}

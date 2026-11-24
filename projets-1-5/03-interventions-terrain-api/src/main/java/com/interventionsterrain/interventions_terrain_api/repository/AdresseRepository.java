package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux adresses d'intervention (E-21).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Adresse.
 * DÉPEND DE    : Spring Data JPA et l'entité Adresse.
 * UTILISÉ PAR  : AdresseService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface AdresseRepository extends JpaRepository<Adresse, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES (héritées de JpaRepository)
    // ═════════════════════════════════════════════════════════════════════════
}

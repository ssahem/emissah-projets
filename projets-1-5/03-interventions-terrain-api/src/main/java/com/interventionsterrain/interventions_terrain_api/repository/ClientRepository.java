package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Client;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux clients (F4).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Client.
 * DÉPEND DE    : Spring Data JPA et l'entité Client.
 * UTILISÉ PAR  : ClientService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface ClientRepository extends JpaRepository<Client, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    List<Client> findAllByOrderByNomAsc();

    boolean existsByEmail(String email);
}

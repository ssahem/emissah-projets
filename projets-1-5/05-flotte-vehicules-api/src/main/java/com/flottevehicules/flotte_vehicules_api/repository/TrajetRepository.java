package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux trajets.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link Trajet}, source du compteur courant avec les restitutions.
 * UTILISÉ PAR  : les vues SQL du compteur.
 * NE FAIT PAS  : de contrôle de cohérence kilométrique.
 */
public interface TrajetRepository extends JpaRepository<Trajet, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

}

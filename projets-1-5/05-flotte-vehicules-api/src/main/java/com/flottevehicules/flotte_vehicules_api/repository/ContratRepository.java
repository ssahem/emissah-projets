package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux contrats de location ou de leasing.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link Contrat}.
 * UTILISÉ PAR  : les vues SQL de coût.
 * NE FAIT PAS  : de calcul de loyer.
 */
public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

}

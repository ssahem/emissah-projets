package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux prestataires d'entretien.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link Prestataire}.
 * UTILISÉ PAR  : EntretienService.
 * NE FAIT PAS  : de règle métier.
 */
public interface PrestataireRepository extends JpaRepository<Prestataire, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

}

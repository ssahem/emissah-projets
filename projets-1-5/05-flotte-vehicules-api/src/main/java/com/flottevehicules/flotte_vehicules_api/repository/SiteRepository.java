package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Site;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des sites.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Site} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : SiteService uniquement.
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface SiteRepository extends JpaRepository<Site, Integer> {

    /** Retourne les sites triés par ville puis par nom (F1). */
    List<Site> findAllByOrderByVilleAscNomAsc();

    /** Cherche un site par son nom unique (F1). */
    Optional<Site> findByNom(String nom);
}

package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux données des véhicules.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Vehicule}, rechercher par critères partiels (F9).
 * UTILISÉ PAR  : VehiculeService, SiteService, AffectationService.
 * NE FAIT PAS  : de calcul de disponibilité ni de conversion en DTO.
 */
public interface VehiculeRepository extends JpaRepository<Vehicule, Integer> {

    /** Retourne les véhicules triés par immatriculation (F2). */
    List<Vehicule> findAllByOrderByImmatriculationAsc();

    /** Retourne les véhicules d'un site triés par immatriculation (F6). */
    List<Vehicule> findAllByIdSiteOrderByImmatriculationAsc(Integer idSite);

    /** Cherche un véhicule par immatriculation unique. */
    Optional<Vehicule> findByImmatriculation(String immatriculation);

    /** Recherche combinée : immatriculation partielle insensible à la casse, marque exacte (F9). */
    @Query("SELECT v FROM Vehicule v WHERE "
            + "(:immatriculation IS NULL OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :immatriculation, '%'))) "
            + "AND (:marque IS NULL OR v.marque = :marque) "
            + "ORDER BY v.immatriculation ASC")
    List<Vehicule> search(@Param("immatriculation") String immatriculation,
                          @Param("marque") String marque);
}

package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux données des entretiens.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Entretien}, interroger les immobilisations (F12,
 *                F17).
 * UTILISÉ PAR  : EntretienService, VehiculeService, AffectationService.
 * NE FAIT PAS  : de calcul de coût cumulé ni de conversion en DTO.
 */
public interface EntretienRepository extends JpaRepository<Entretien, Integer> {

    /** Retourne l'historique d'un véhicule trié par date de début décroissante (F8). */
    List<Entretien> findAllByIdVehiculeOrderByDateDebutDesc(Integer idVehicule);

    /** Indique si un véhicule a au moins un entretien (F2). */
    boolean existsByIdVehicule(Integer idVehicule);

    /**
     * Immobilisations recouvrant la période demandée (F12, F17).
     * Une date de fin absente signifie une immobilisation d'un seul jour (D-02).
     */
    @Query("SELECT e FROM Entretien e WHERE e.idVehicule = :idVehicule "
            + "AND e.dateDebut <= :fin AND :debut <= COALESCE(e.dateFin, e.dateDebut) "
            + "ORDER BY e.dateDebut ASC")
    List<Entretien> findImmobilisationsChevauchant(@Param("idVehicule") Integer idVehicule,
                                                   @Param("debut") LocalDate debut,
                                                   @Param("fin") LocalDate fin);
}

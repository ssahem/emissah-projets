package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux données des affectations.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Affectation}, interroger les chevauchements (F11,
 *                F17).
 * UTILISÉ PAR  : AffectationService, SalarieService, SiteService, VehiculeService.
 * NE FAIT PAS  : de décision métier sur le chevauchement.
 */
public interface AffectationRepository extends JpaRepository<Affectation, Integer> {

    /** Retourne les affectations triées par date de début décroissante (F4, F7). */
    List<Affectation> findAllByOrderByDateDebutDesc();

    /** Retourne les affectations d'un salarié, de la plus récente à la plus ancienne (F7). */
    List<Affectation> findAllByIdSalarieOrderByDateDebutDesc(Integer idSalarie);

    /** Retourne les affectations d'un véhicule. */
    List<Affectation> findAllByIdVehicule(Integer idVehicule);

    /** Indique si un véhicule a au moins une affectation (F2). */
    boolean existsByIdVehicule(Integer idVehicule);

    /** Indique si un salarié a une affectation sans date de fin (F3). */
    boolean existsByIdSalarieAndDateFinIsNull(Integer idSalarie);

    /** Affectations recouvrant la période demandée sur un véhicule (F11, F17). */
    @Query("SELECT a FROM Affectation a WHERE a.idVehicule = :idVehicule "
            + "AND a.dateDebut <= :fin AND (:debut <= a.dateFin OR a.dateFin IS NULL) "
            + "ORDER BY a.dateDebut ASC")
    List<Affectation> findChevauchements(@Param("idVehicule") Integer idVehicule,
                                         @Param("debut") LocalDate debut,
                                         @Param("fin") LocalDate fin);

    /** Relevés de restitution clôturés sur un intervalle, pour F19. */
    @Query("SELECT a FROM Affectation a WHERE a.idVehicule IN :idsVehicule "
            + "AND a.dateFin BETWEEN :debut AND :fin AND a.kmRestitution IS NOT NULL")
    List<Affectation> findRestitutionsEntre(@Param("idsVehicule") List<Integer> idsVehicule,
                                            @Param("debut") LocalDate debut,
                                            @Param("fin") LocalDate fin);
}

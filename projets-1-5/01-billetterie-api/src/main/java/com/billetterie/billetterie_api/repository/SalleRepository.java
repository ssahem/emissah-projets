package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Salle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux salles. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface SalleRepository extends JpaRepository<Salle, Integer> {

    List<Salle> findAllByOrderByNomAscVilleAsc();

    boolean existsByNomIgnoreCaseAndVilleIgnoreCase(String nom, String ville);

    boolean existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdSalleNot(
            String nom, String ville, Integer idSalle);

    @Query("select count(p) from Place p where p.salle.idSalle = :idSalle")
    long countPlacesByIdSalle(@Param("idSalle") Integer idSalle);

    @Query("select count(e) from Evenement e where e.salle.idSalle = :idSalle")
    long countEvenementsByIdSalle(@Param("idSalle") Integer idSalle);
}

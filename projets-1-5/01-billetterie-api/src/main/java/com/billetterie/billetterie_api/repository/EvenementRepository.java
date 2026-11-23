package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Evenement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux événements. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface EvenementRepository extends JpaRepository<Evenement, Integer> {

    List<Evenement> findAllByOrderByDateHeureAsc();

    List<Evenement> findAllByDateHeureGreaterThanEqualOrderByDateHeureAsc(LocalDateTime du);

    List<Evenement> findAllByDateHeureLessThanEqualOrderByDateHeureAsc(LocalDateTime au);

    List<Evenement> findAllByDateHeureBetweenOrderByDateHeureAsc(LocalDateTime du, LocalDateTime au);

    List<Evenement> findAllBySalle_IdSalleOrderByDateHeureAsc(Integer idSalle);

    boolean existsBySalle_IdSalle(Integer idSalle);

    @Query("select count(r) from Reservation r where r.evenement.idEvenement = :idEvenement")
    long countReservationsByIdEvenement(@Param("idEvenement") Integer idEvenement);

    @Query("select count(p) from Place p where p.salle.idSalle = :idSalle")
    long countPlacesByIdSalle(@Param("idSalle") Integer idSalle);
}

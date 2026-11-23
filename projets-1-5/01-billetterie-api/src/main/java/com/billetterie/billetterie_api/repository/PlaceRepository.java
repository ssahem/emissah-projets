package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux places. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface PlaceRepository extends JpaRepository<Place, Integer> {

    List<Place> findAllByOrderByRangAscNumeroAsc();

    List<Place> findAllBySalle_IdSalleOrderByRangAscNumeroAsc(Integer idSalle);

    List<Place> findAllBySalle_IdSalleAndCategorie_LibelleIgnoreCaseOrderByRangAscNumeroAsc(
            Integer idSalle, String libelle);

    Optional<Place> findBySalle_IdSalleAndRangAndNumero(Integer idSalle, String rang, Integer numero);

    Optional<Place> findFirstByRangAndNumero(String rang, Integer numero);

    boolean existsBySalle_IdSalleAndRangAndNumero(Integer idSalle, String rang, Integer numero);

    long countBySalle_IdSalle(Integer idSalle);
}

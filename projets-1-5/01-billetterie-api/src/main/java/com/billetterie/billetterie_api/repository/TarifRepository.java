package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Tarif;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux tarifs. COUCHE : Repository · NE FAIT PAS : de calcul de prix. */
public interface TarifRepository extends JpaRepository<Tarif, Integer> {
    Optional<Tarif> findByEvenement_IdEvenementAndCategorieIgnoreCase(
            Integer idEvenement, String categorie);
}

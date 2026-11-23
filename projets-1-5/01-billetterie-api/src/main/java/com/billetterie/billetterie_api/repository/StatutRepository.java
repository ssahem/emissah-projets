package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Statut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux statuts. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface StatutRepository extends JpaRepository<Statut, Integer> {
    Optional<Statut> findByLibelleIgnoreCase(String libelle);
}

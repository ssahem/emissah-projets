package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Categorie;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux catégories. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
    Optional<Categorie> findByLibelleIgnoreCase(String libelle);
    boolean existsByLibelleIgnoreCase(String libelle);
}

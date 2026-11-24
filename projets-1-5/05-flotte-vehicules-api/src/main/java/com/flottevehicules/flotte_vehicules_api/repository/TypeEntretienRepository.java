package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.TypeEntretien;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux types d'entretien.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire des {@link TypeEntretien}.
 * UTILISÉ PAR  : EntretienService, AlerteService.
 * NE FAIT PAS  : de calcul d'échéance.
 */
public interface TypeEntretienRepository extends JpaRepository<TypeEntretien, Integer> {

    /** Retourne les types dont la catégorie est PERIODIQUE (F15). */
    List<TypeEntretien> findAllByCategorie(String categorie);
}

package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Matiere;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux matières (F3).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Matiere}.
 * UTILISÉ PAR  : MatiereService, RestitutionService, ReglesMetierService.
 * NE FAIT PAS  : de règle métier.
 */
public interface MatiereRepository extends JpaRepository<Matiere, Integer> {

    /** Retourne les matières triées par libellé. */
    List<Matiere> findAllByOrderByLibelleAsc();

    /** Cherche une matière par libellé (unicité). */
    Optional<Matiere> findByLibelle(String libelle);
}

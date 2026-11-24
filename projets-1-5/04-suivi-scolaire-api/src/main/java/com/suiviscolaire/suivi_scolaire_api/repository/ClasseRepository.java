package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Classe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux classes (F1).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Classe} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : ClasseService, EleveService, ReglesMetierService.
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ClasseRepository extends JpaRepository<Classe, Integer> {

    /** Retourne les classes triées par niveau puis par nom. */
    List<Classe> findAllByOrderByIdNiveauAscNomAsc();

    /** Cherche une classe par nom et niveau (unicité métier). */
    Optional<Classe> findByNomAndIdNiveau(String nom, Integer idNiveau);
}

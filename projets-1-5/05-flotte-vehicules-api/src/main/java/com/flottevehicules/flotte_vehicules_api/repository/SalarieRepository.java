package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des salariés.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Salarie}.
 * UTILISÉ PAR  : SalarieService, AffectationService.
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface SalarieRepository extends JpaRepository<Salarie, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne les salariés triés par nom (F3). */
    List<Salarie> findAllByOrderByNomAsc();

    /** Cherche un salarié par matricule unique. */
    Optional<Salarie> findByMatricule(String matricule);

    /** Cherche un salarié par adresse électronique unique. */
    Optional<Salarie> findByEmail(String email);
}

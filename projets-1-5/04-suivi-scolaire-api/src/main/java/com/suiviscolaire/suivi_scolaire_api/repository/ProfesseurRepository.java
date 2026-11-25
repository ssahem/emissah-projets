package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Professeur;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux professeurs (F4).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Professeur}.
 * UTILISÉ PAR  : ProfesseurService.
 * NE FAIT PAS  : de règle métier.
 */
public interface ProfesseurRepository extends JpaRepository<Professeur, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne les professeurs triés par nom puis prénom. */
    List<Professeur> findAllByOrderByNomAscPrenomAsc();

    /** Cherche un professeur par email (unicité). */
    Optional<Professeur> findByEmail(String email);
}

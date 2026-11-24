package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.TypeEvaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux types d'évaluation (F5, F13).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link TypeEvaluation}.
 * UTILISÉ PAR  : ReglesMetierService, NoteService, ConsultationService.
 * NE FAIT PAS  : de règle métier.
 */
public interface TypeEvaluationRepository extends JpaRepository<TypeEvaluation, Integer> {

    /** Cherche un type par son libellé exact. */
    Optional<TypeEvaluation> findByLibelle(String libelle);

    /** Cherche un type par son libellé sans tenir compte de la casse (alias F13). */
    Optional<TypeEvaluation> findByLibelleIgnoreCase(String libelle);
}

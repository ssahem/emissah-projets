package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Enseignement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux enseignements (F8, F12, F14).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : retrouver les affectations matière/professeur par classe.
 * UTILISÉ PAR  : ReglesMetierService, ConsultationService, NoteService, RestitutionService.
 * NE FAIT PAS  : de règle métier.
 */
public interface EnseignementRepository extends JpaRepository<Enseignement, Integer> {

    /** Enseignements d'une classe, triés par identifiant. */
    List<Enseignement> findAllByIdClasseOrderByIdEnseignementAsc(Integer idClasse);

    /** Enseignement d'une matière dans une classe (F8, F12). */
    Optional<Enseignement> findByIdClasseAndIdMatiere(Integer idClasse, Integer idMatiere);

    /** Matières distinctes enseignées par un professeur (A-02). */
    @org.springframework.data.jpa.repository.Query(
            "select distinct e.idMatiere from Enseignement e where e.idProfesseur = :idProfesseur order by e.idMatiere")
    List<Integer> findDistinctMatiereByIdProfesseur(
            @org.springframework.data.repository.query.Param("idProfesseur") Integer idProfesseur);

    /** Vérifie si un professeur est affecté à une classe (F4). */
    boolean existsByIdProfesseur(Integer idProfesseur);

    /** Vérifie si une matière est enseignée (F3). */
    boolean existsByIdMatiere(Integer idMatiere);
}

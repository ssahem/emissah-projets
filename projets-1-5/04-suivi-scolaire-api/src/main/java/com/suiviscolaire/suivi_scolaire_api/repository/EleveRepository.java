package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Eleve;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux élèves (F2, F6, F9).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Eleve}, avec recherche partielle insensible à la
 *                casse.
 * UTILISÉ PAR  : EleveService, ConsultationService, RestitutionService.
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface EleveRepository extends JpaRepository<Eleve, Integer> {

    /** Retourne tous les élèves triés par nom puis prénom. */
    List<Eleve> findAllByOrderByNomAscPrenomAsc();

    /** Recherche partielle insensible à la casse sur le nom (F9). */
    @Query("select e from Eleve e where lower(e.nom) like lower(concat('%', :nom, '%')) order by e.nom, e.prenom")
    List<Eleve> searchByNom(@Param("nom") String nom);
}

package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Trimestre;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Accès aux trimestres (F5, F13, F14, F15, F16).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : retrouver un trimestre par numéro et connaître son état de clôture.
 * UTILISÉ PAR  : NoteService, ReglesMetierService, AlertesDifficulteService.
 * NE FAIT PAS  : de règle métier.
 */
public interface TrimestreRepository extends JpaRepository<Trimestre, Integer> {

    /** Retourne les trimestres triés par numéro. */
    List<Trimestre> findAllByOrderByNumeroAsc();

    /** Trimestre par numéro (les numéros 1 à 3 sont uniques toutes années confondues en démo). */
    Optional<Trimestre> findByNumero(Integer numero);

    /** Dernier trimestre clôturé, selon l'année puis le numéro (F15). */
    @Query("select t from Trimestre t where t.estCloture = true order by t.idAnnee desc, t.numero desc")
    List<Trimestre> findCloturesDuPlusRecentAuPlusAncien();

    /** Existe-t-il au moins un trimestre clôturé (F13) ? */
    boolean existsByEstClotureTrue();

    /** Trimestre contenant une date (détermination du trimestre en cours, F16). */
    @Query("select t from Trimestre t where :date between t.dateDebut and t.dateFin order by t.numero desc")
    List<Trimestre> findContenant(@org.springframework.data.repository.query.Param("date") java.time.LocalDate date);
}

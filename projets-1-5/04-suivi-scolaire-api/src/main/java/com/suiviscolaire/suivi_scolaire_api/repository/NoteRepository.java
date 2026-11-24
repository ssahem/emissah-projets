package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.Note;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux notes (F5, F7, F8, F10, F14, F17).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Note}, sans FK vers inscription ni trimestre (A-01,
 *                A-03).
 * UTILISÉ PAR  : NoteService, ConsultationService, ReglesMetierService.
 * NE FAIT PAS  : de règle métier ni de calcul de moyenne (réalisé via les vues).
 */
public interface NoteRepository extends JpaRepository<Note, Integer> {

    /** Notes d'une inscription, triées par trimestre puis matière. */
    @Query("select n from Note n where n.numeroInscription = :numero "
            + "and (:trimestre is null or n.numeroTrimestre = :trimestre) "
            + "order by n.numeroTrimestre, n.idEnseignement, n.idTypeEval")
    List<Note> findByNumeroInscription(@Param("numero") String numero,
                                       @Param("trimestre") Integer trimestre);

    /** Toutes les notes groupées par matière puis trimestre (F5, F10). */
    List<Note> findAllByOrderByIdEnseignementAscNumeroTrimestreAsc();

    /** Notes d'une matière dans une classe, rattachées aux inscriptions de la classe. */
    @Query("select n from Note n where n.idEnseignement = :idEnseignement "
            + "and n.numeroTrimestre = :trimestre order by n.numeroInscription, n.idTypeEval")
    List<Note> findByIdEnseignementAndTrimestre(@Param("idEnseignement") Integer idEnseignement,
                                                @Param("trimestre") Integer trimestre);

    /** Page de notes (F10). */
    Page<Note> findAllBy(Pageable pageable);

    /** Nombre de notes prises en compte pour une matière et un trimestre (F14, toutes inscriptions). */
    long countByIdEnseignementAndNumeroTrimestre(Integer idEnseignement, Integer numeroTrimestre);

    /** Nombre de notes de l'élève prises en compte pour une matière et un trimestre (F17). */
    long countByIdEnseignementAndNumeroTrimestreAndNumeroInscription(
            Integer idEnseignement, Integer numeroTrimestre, String numeroInscription);

    /** Notes d'un enseignement pour un trimestre, toutes inscriptions confondues (F14). */
    @Query("select case when count(n) > 0 then true else false end from Note n "
            + "where n.idEnseignement = :idEnseignement and n.numeroTrimestre = :trimestre")
    boolean existsByIdEnseignementAndTrimestre(@Param("idEnseignement") Integer idEnseignement,
                                               @Param("trimestre") Integer trimestre);
}

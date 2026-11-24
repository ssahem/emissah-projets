package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.ObtentionCompetence;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Compétences obtenues (F3, F6, F7, F16). */
public interface ObtentionCompetenceRepository extends JpaRepository<ObtentionCompetence, Integer> {

    boolean existsByIdCompetence(Integer idCompetence);

    List<ObtentionCompetence> findAllByIdTechnicienOrderByDateObtentionDesc(Integer idTechnicien);

    /** Compte les compétences distinctes encore valides à une date (F6). */
    @Query("select count(distinct o.idCompetence) from ObtentionCompetence o "
            + "where o.idTechnicien = :idTechnicien "
            + "and (o.dateExpiration is null or o.dateExpiration >= :date)")
    long countCompetencesValides(@Param("idTechnicien") Integer idTechnicien,
                                 @Param("date") LocalDate date);

    /** F16 : compétences arrivant à expiration et jamais encore signalées. */
    List<ObtentionCompetence> findAllByDateExpirationBetweenAndDateAlerteIsNull(
            LocalDate debut, LocalDate fin);

    /** F11 : doublon de compétence active (sans expiration ou expiration future). */
    @Query("select count(o) > 0 from ObtentionCompetence o "
            + "where o.idTechnicien = :idTechnicien and o.idCompetence = :idCompetence "
            + "and (o.dateExpiration is null or o.dateExpiration >= :date)")
    boolean existsCompetenceActive(@Param("idTechnicien") Integer idTechnicien,
                                   @Param("idCompetence") Integer idCompetence,
                                   @Param("date") LocalDate date);
}

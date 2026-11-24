package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès à l'historique des statuts : le dernier enregistrement fait foi (A-03). */
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Integer> {

    Optional<HistoriqueStatut> findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(Integer idAffectation);

    @Modifying
    @Query("delete from HistoriqueStatut h where h.idAffectation = :idAffectation")
    void deleteByIdAffectation(@Param("idAffectation") Integer idAffectation);
}

package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Affectation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux affectations (A-02, A-03).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Affectation.
 * DÉPEND DE    : Spring Data JPA et l'entité Affectation.
 * UTILISÉ PAR  : AffectationService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface AffectationRepository extends JpaRepository<Affectation, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    Optional<Affectation> findByIdInterventionAndEstResponsableTrue(Integer idIntervention);

    List<Affectation> findAllByIdIntervention(Integer idIntervention);

    Optional<Affectation> findByIdInterventionAndIdTechnicien(Integer idIntervention, Integer idTechnicien);

    boolean existsByIdTechnicien(Integer idTechnicien);

    List<Affectation> findAllByIdTechnicien(Integer idTechnicien);

    @Modifying
    @Query("delete from Affectation a where a.idIntervention = :idIntervention")
    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @QUERY
    // ═════════════════════════════════════════════════════════════════════════

    void deleteByIdIntervention(@Param("idIntervention") Integer idIntervention);
}

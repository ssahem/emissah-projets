package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.HistoriqueStatut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès à l'historique des statuts : le dernier enregistrement fait foi (A-03).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à HistoriqueStatut.
 * DÉPEND DE    : Spring Data JPA et l'entité HistoriqueStatut.
 * UTILISÉ PAR  : HistoriqueStatutService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    Optional<HistoriqueStatut> findTopByIdAffectationOrderByDateHeureDescIdHistoriqueDesc(Integer idAffectation);

    @Modifying
    @Query("delete from HistoriqueStatut h where h.idAffectation = :idAffectation")
    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @QUERY
    // ═════════════════════════════════════════════════════════════════════════

    // ── deleteByIdAffectation() ──────────────────────────────────────────────

    /**
     * Exécute deleteByIdAffectation().
     */

    void deleteByIdAffectation(@Param("idAffectation") Integer idAffectation);
}

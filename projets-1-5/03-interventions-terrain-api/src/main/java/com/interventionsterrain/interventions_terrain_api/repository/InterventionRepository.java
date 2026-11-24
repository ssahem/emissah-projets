package com.interventionsterrain.interventions_terrain_api.repository;

import com.interventionsterrain.interventions_terrain_api.entity.Intervention;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux interventions (F5, F8, F9, F10).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : exposer les requêtes JPA sur la table associée à Intervention.
 * DÉPEND DE    : Spring Data JPA et l'entité Intervention.
 * UTILISÉ PAR  : InterventionService.
 * NE FAIT PAS  : de logique métier ni de HTTP.
 */
public interface InterventionRepository extends JpaRepository<Intervention, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    List<Intervention> findAllByOrderByDebutPrevuAsc();

    boolean existsByIdClient(Integer idClient);

    List<Intervention> findAllByIdClientOrderByDateDemandeDesc(Integer idClient);

    List<Intervention> findAllByIdInterventionInOrderByDebutPrevuAsc(Collection<Integer> ids);

    /**
     * Filtre F9 : le statut est lu sur le dernier historique de l'affectation responsable (E-12),
     * pas sur la vue qui agrège toutes les affectations (A-02).
     */
    @Query(value = """
            SELECT i.* FROM intervention i
            JOIN affectation a ON a.id_intervention = i.id_intervention AND a.est_responsable
            JOIN LATERAL (
                SELECT h.id_statut FROM historique_statut h
                WHERE h.id_affectation = a.id_affectation
                ORDER BY h.date_heure DESC, h.id_historique DESC
                LIMIT 1
            ) h ON true
            JOIN statut s ON s.id_statut = h.id_statut
            WHERE (CAST(:statut AS varchar) IS NULL OR s.libelle = CAST(:statut AS varchar))
              AND (CAST(:du AS timestamp) IS NULL OR i.debut_prevu >= CAST(:du AS timestamp))
              AND (CAST(:au AS timestamp) IS NULL OR i.debut_prevu < CAST(:au AS timestamp))
            ORDER BY i.debut_prevu ASC, i.id_intervention ASC
            """, nativeQuery = true)
    List<Intervention> findAllFiltres(@Param("statut") String statut,
                                      @Param("du") LocalDateTime du,
                                      @Param("au") LocalDateTime au);

    boolean existsByIdInterventionOrigine(Integer idInterventionOrigine);

    /** F12 : chevauchements sur toutes les affectations du technicien, hors intervention exclue. */
    @Query(value = """
            SELECT i.* FROM intervention i
            JOIN affectation a ON a.id_intervention = i.id_intervention
            WHERE a.id_technicien = :idTechnicien
              AND i.id_intervention <> :idExclu
              AND i.debut_prevu < :fin
              AND i.fin_prevue > :debut
            ORDER BY i.debut_prevu ASC
            """, nativeQuery = true)
    List<Intervention> findChevauchements(@Param("idTechnicien") Integer idTechnicien,
                                          @Param("debut") LocalDateTime debut,
                                          @Param("fin") LocalDateTime fin,
                                          @Param("idExclu") Integer idExclu);
}

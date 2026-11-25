package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Appartement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux données des appartements (F3).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Appartement} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : AppartementService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface AppartementRepository extends JpaRepository<Appartement, Integer>,
        AppartementSearchRepository {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByEtageAscSurfaceAsc() ─────────────────────────────────

    /** Retourne les appartements triés par étage, puis par surface croissante. */
    List<Appartement> findAllByOrderByEtageAscSurfaceAsc();

    // ── findAllByIdImmeubleOrderByEtageAscSurfaceAsc() ───────────────────────

    /** Retourne les appartements d'un immeuble pour la consultation F6. */
    List<Appartement> findAllByIdImmeubleOrderByEtageAscSurfaceAsc(Integer idImmeuble);

    // ── findByNumeroAndIdImmeuble() ──────────────────────────────────────────

    /** Cherche un appartement par numéro dans un immeuble (clé métier). */
    Optional<Appartement> findByNumeroAndIdImmeuble(String numero, Integer idImmeuble);

    // ── findMaxEtageByIdImmeuble() ───────────────────────────────────────────

    /** Retourne l'étage le plus élevé d'un immeuble, ou null s'il est vide. */
    @Query("SELECT MAX(a.etage) FROM Appartement a WHERE a.idImmeuble = :idImmeuble")
    Integer findMaxEtageByIdImmeuble(@Param("idImmeuble") Integer idImmeuble);

    // ── countByIdImmeuble() ──────────────────────────────────────────────────

    /** Compte les appartements d'un immeuble, pour refuser sa suppression (F2). */
    long countByIdImmeuble(Integer idImmeuble);
}

package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.HistoriqueBail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès à la traçabilité du cycle de vie des baux (V5).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire les événements RESILIATION, RENOUVELLEMENT et CLOTURE.
 * UTILISÉ PAR  : BailService et BailClotureService (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface HistoriqueBailRepository extends JpaRepository<HistoriqueBail, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByIdBailOrderByDateEvenementDesc() ───────────────────────────────

    /** Retourne les événements d'un bail du plus récent au plus ancien. */
    List<HistoriqueBail> findByIdBailOrderByDateEvenementDesc(Integer idBail);

    // ── deleteByIdBail() ─────────────────────────────────────────────────────

    /** Supprime les événements d'un bail avant sa suppression (F5). */
    void deleteByIdBail(Integer idBail);
}

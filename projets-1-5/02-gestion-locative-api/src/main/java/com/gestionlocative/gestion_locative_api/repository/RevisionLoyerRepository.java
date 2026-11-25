package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.RevisionLoyer;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès à l'historique des loyers (V6).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire les révisions de loyer d'un bail.
 * UTILISÉ PAR  : BailService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface RevisionLoyerRepository extends JpaRepository<RevisionLoyer, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByIdBailOrderByDateEffetDesc() ───────────────────────────────────

    /** Retourne les révisions d'un bail, de la plus récente à la plus ancienne. */
    List<RevisionLoyer> findByIdBailOrderByDateEffetDesc(Integer idBail);

    // ── findByIdBailOrderByDateEffetAsc() ────────────────────────────────────

    /** Retourne les paliers de loyer dans l'ordre chronologique (F18, F19). */
    List<RevisionLoyer> findByIdBailOrderByDateEffetAsc(Integer idBail);

    // ── existsByIdBailAndDateEffet() ─────────────────────────────────────────

    /** Vrai si une révision existe déjà à cette date pour ce bail (F15). */
    boolean existsByIdBailAndDateEffet(Integer idBail, LocalDate dateEffet);

    // ── deleteByIdBail() ─────────────────────────────────────────────────────

    /**
     * Supprime les révisions d'un bail avant sa suppression (F5).
     */

    void deleteByIdBail(Integer idBail);
}

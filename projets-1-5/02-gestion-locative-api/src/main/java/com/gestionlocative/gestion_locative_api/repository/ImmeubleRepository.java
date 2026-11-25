package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Immeuble;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des immeubles (F2).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Immeuble} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : ImmeubleService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ImmeubleRepository extends JpaRepository<Immeuble, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByAdresseAsc() ─────────────────────────────────────────

    /** Retourne les immeubles triés par adresse. */
    List<Immeuble> findAllByOrderByAdresseAsc();

    // ── findAllByIdProprietaireOrderByAdresseAsc() ───────────────────────────

    /** Retourne les immeubles d'un propriétaire pour la consultation F7. */
    List<Immeuble> findAllByIdProprietaireOrderByAdresseAsc(Integer idProprietaire);

    // ── countByIdProprietaire() ──────────────────────────────────────────────

    /** Compte les immeubles d'un propriétaire, pour refuser sa suppression (F1). */
    long countByIdProprietaire(Integer idProprietaire);
}

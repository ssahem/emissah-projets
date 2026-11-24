package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Proprietaire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des propriétaires (F1).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Proprietaire} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : ProprietaireService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ProprietaireRepository extends JpaRepository<Proprietaire, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByNomAsc() ────────────────────────────────────────────

    /** Retourne les propriétaires triés par nom, puis par prénom. */
    List<Proprietaire> findAllByOrderByNomAscPrenomAsc();

    // ── findByEmail() ───────────────────────────────────────────────────────

    /** Cherche un propriétaire par email (unicité métier). */
    Optional<Proprietaire> findByEmail(String email);
}

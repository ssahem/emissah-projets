package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Locataire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données des locataires (F4).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Locataire} ; Spring Data génère l'implémentation.
 * UTILISÉ PAR  : LocataireService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface LocataireRepository extends JpaRepository<Locataire, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByNomAsc() ─────────────────────────────────────────────

    /** Retourne les locataires triés par nom, puis par prénom. */
    List<Locataire> findAllByOrderByNomAscPrenomAsc();

    // ── findByEmail() ────────────────────────────────────────────────────────

    /** Cherche un locataire par email (unicité métier). */
    Optional<Locataire> findByEmail(String email);
}

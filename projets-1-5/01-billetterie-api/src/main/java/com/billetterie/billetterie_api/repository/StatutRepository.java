package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Statut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux statuts.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Statut ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface StatutRepository extends JpaRepository<Statut, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByLibelleIgnoreCase() ────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    Optional<Statut> findByLibelleIgnoreCase(String libelle);
}

package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux clients.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Client ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface ClientRepository extends JpaRepository<Client, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByEmailIgnoreCase() ──────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    Optional<Client> findByEmailIgnoreCase(String email);
    // ── existsByEmailIgnoreCase() ────────────────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    boolean existsByEmailIgnoreCase(String email);
}

package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Spectacle;
import com.billetterie.billetterie_api.enums.TypeSpectacle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux spectacles.
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des Spectacle ; Spring Data génère l’implémentation.
 * UTILISÉ PAR  : les services uniquement (jamais un controller).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface SpectacleRepository extends JpaRepository<Spectacle, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findFirstByTitreIgnoreCaseAndType() ──────────────────────────────

    /** Requête Spring Data déduite du nom de la méthode. */
    Optional<Spectacle> findFirstByTitreIgnoreCaseAndType(String titre, TypeSpectacle type);
}

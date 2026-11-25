package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.BailLocataire;
import com.gestionlocative.gestion_locative_api.entity.BailLocataireId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données de la liaison bail ↔ locataire (E-08).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire les liaisons, en conserveant la clé composite du schéma source.
 * UTILISÉ PAR  : BailService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface BailLocataireRepository extends JpaRepository<BailLocataire, BailLocataireId> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findByIdIdBail() ─────────────────────────────────────────────────────

    /** Retourne les liaisons d'un bail, la première étant le locataire principal. */
    List<BailLocataire> findByIdIdBail(Integer idBail);

    // ── deleteByIdIdBail() ───────────────────────────────────────────────────

    /** Supprime les liaisons d'un bail avant la suppression du bail lui-même (F5). */
    void deleteByIdIdBail(Integer idBail);
}

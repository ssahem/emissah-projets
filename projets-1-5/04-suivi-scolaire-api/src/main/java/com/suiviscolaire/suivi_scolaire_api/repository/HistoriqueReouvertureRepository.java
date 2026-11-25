package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.HistoriqueReouverture;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès au journal des réouvertures (F15).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire les traces de réouverture de trimestre.
 * UTILISÉ PAR  : ReglesMetierService.
 * NE FAIT PAS  : de règle métier.
 */
public interface HistoriqueReouvertureRepository extends JpaRepository<HistoriqueReouverture, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    /** Trace éventuelle d'une réouverture du trimestre. */
    Optional<HistoriqueReouverture> findByIdTrimestre(Integer idTrimestre);
}

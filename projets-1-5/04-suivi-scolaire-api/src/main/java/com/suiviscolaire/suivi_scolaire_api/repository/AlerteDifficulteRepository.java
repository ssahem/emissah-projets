package com.suiviscolaire.suivi_scolaire_api.repository;

import com.suiviscolaire.suivi_scolaire_api.entity.AlerteDifficulte;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux alertes de difficulté (F16).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire les alertes, avec unicité par inscription et trimestre.
 * UTILISÉ PAR  : AlertesDifficulteService, AlerteController.
 * NE FAIT PAS  : de calcul ni de décision de signalement.
 */
public interface AlerteDifficulteRepository extends JpaRepository<AlerteDifficulte, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    /** Vérifie qu'un élève n'a pas déjà été signalé pour ce trimestre. */
    Optional<AlerteDifficulte> findByIdInscriptionAndNumeroTrimestre(Integer idInscription,
                                                                     Integer numeroTrimestre);

    /** Alertes d'un trimestre, triées par identifiant. */
    List<AlerteDifficulte> findAllByNumeroTrimestreOrderByIdAlerteAsc(Integer numeroTrimestre);
}

package com.flottevehicules.flotte_vehicules_api.repository;

import com.flottevehicules.flotte_vehicules_api.entity.AlerteVehicule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux alertes persistantes (F12, F16).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link AlerteVehicule}.
 * UTILISÉ PAR  : AlerteService, EntretienService.
 * NE FAIT PAS  : de génération d'alerte ni de sérialisation HTTP.
 */
public interface AlerteRepository extends JpaRepository<AlerteVehicule, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    /** Retourne toutes les alertes, les plus récentes d'abord. */
    List<AlerteVehicule> findAllByOrderByDateAlerteDescIdAlerteDesc();

    /** Indique si une échéance donnée a déjà été signalée pour un véhicule (F16). */
    boolean existsByIdVehiculeAndMotifAndDateEcheance(Integer idVehicule, String motif,
                                                      LocalDate dateEcheance);
}

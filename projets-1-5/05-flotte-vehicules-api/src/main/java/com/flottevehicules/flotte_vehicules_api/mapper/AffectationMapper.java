package com.flottevehicules.flotte_vehicules_api.mapper;

import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationResponse;
import com.flottevehicules.flotte_vehicules_api.dto.response.AffectationSalarieResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Affectation;
import com.flottevehicules.flotte_vehicules_api.entity.Salarie;
import com.flottevehicules.flotte_vehicules_api.entity.Vehicule;
import com.flottevehicules.flotte_vehicules_api.repository.SalarieRepository;
import com.flottevehicules.flotte_vehicules_api.repository.VehiculeRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Conversion Affectation → réponses F4 et F7.
 *
 * COUCHE       : Mapper (interface HTTP)
 * RÔLE         : Conversion Affectation → réponses F4 et F7.
 * DÉPEND DE    : VehiculeRepository, SalarieRepository, Clock (signalement de l'affectation en
 *                cours).
 * NE FAIT PAS  : de contrôle de chevauchement.
 */
@Component
public class AffectationMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final VehiculeRepository vehiculeRepository;
    private final SalarieRepository salarieRepository;
    private final Clock clock;

    public AffectationMapper(VehiculeRepository vehiculeRepository,
                             SalarieRepository salarieRepository, Clock clock) {
        this.vehiculeRepository = vehiculeRepository;
        this.salarieRepository = salarieRepository;
        this.clock = clock;
    }

    /** Convertit une affectation en réponse complète (F4, F11). */

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    public AffectationResponse toResponse(Affectation affectation) {
        String immatriculation = vehiculeRepository.findById(affectation.getIdVehicule())
                .map(Vehicule::getImmatriculation).orElse(null);
        String nomSalarie = salarieRepository.findById(affectation.getIdSalarie())
                .map(s -> s.getPrenom() + " " + s.getNom()).orElse(null);
        return new AffectationResponse(
                affectation.getIdAffectation(), affectation.getDateDebut(), affectation.getDateFin(),
                affectation.getIdVehicule(), immatriculation, affectation.getIdSalarie(), nomSalarie,
                affectation.getKmRestitution(), affectation.estEnCours(LocalDate.now(clock)));
    }

    /** Convertit une affectation en réponse condensée pour une fiche salarié (F7). */

    // ── toSalarieResponse() ──────────────────────────────────────────────────

    /**
     * Exécute toSalarieResponse().
     */

    public AffectationSalarieResponse toSalarieResponse(Affectation affectation) {
        return new AffectationSalarieResponse(
                affectation.getIdAffectation(), affectation.getIdVehicule(),
                Optional.ofNullable(vehiculeRepository.findById(affectation.getIdVehicule()).orElse(null))
                        .map(Vehicule::getImmatriculation).orElse(null),
                affectation.getDateDebut(), affectation.getDateFin(),
                affectation.estEnCours(LocalDate.now(clock)));
    }
}

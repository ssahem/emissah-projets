package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Alerte d'échéance (F16) ou de conflit d'immobilisation (F12).
 *
 * COUCHE       : DTO
 * RÔLE         : Alerte d'échéance (F16) ou de conflit d'immobilisation (F12).
 * NE FAIT PAS  : de logique métier ni d’accès base.
 */
@Schema(description = "Alerte produite par la tâche quotidienne ou lors d'un entretien")
public record AlerteResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’alerte
        @Schema(description = "Identifiant", example = "1")
        Integer idAlerte,
        // Identifiant du véhicule
        @Schema(description = "Véhicule concerné", example = "2")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "CD-456-EF")
        String immatriculation,
        // Nom du site courant
        @Schema(description = "Site courant", example = "Paris Centre")
        String nomSite,
        // Identifiant de l’affectation
        @Schema(description = "Affectation liée, si elle existe", example = "2")
        Integer idAffectation,
        // Motif
        @Schema(description = "Motif : ECHEANCE_ENTRETIEN ou CONFLIT_AFFECTATION_ENTRETIEN")
        String motif,
        // Période
        @Schema(description = "Période d'émission au format yyyy-MM", example = "2026-11")
        String periode,
        // Date alerte
        @Schema(description = "Date d'émission", example = "2026-11-24")
        LocalDate dateAlerte,
        // Date echeance
        @Schema(description = "Date d'échéance", example = "2026-11-30")
        LocalDate dateEcheance,
        // Ecart Kilométrage
        @Schema(description = "Écart kilométrique constaté", example = "8500")
        Integer ecartKm,
        // Ecart jours
        @Schema(description = "Écart en jours constaté", example = "30")
        Integer ecartJours,
        // Detail
        @Schema(description = "Détail lisible", example = "Échéance dépassée")
        String detail
) {

}

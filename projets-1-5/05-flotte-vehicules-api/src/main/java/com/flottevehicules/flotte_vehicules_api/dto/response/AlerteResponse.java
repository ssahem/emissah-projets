package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** Alerte d'échéance (F16) ou de conflit d'immobilisation (F12). */
@Schema(description = "Alerte produite par la tâche quotidienne ou lors d'un entretien")
public record AlerteResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idAlerte,

        @Schema(description = "Véhicule concerné", example = "2")
        Integer idVehicule,

        @Schema(description = "Immatriculation", example = "CD-456-EF")
        String immatriculation,

        @Schema(description = "Site courant", example = "Paris Centre")
        String nomSite,

        @Schema(description = "Affectation liée, si elle existe", example = "2")
        Integer idAffectation,

        @Schema(description = "Motif : ECHEANCE_ENTRETIEN ou CONFLIT_AFFECTATION_ENTRETIEN")
        String motif,

        @Schema(description = "Période d'émission au format yyyy-MM", example = "2026-11")
        String periode,

        @Schema(description = "Date d'émission", example = "2026-11-24")
        LocalDate dateAlerte,

        @Schema(description = "Date d'échéance", example = "2026-11-30")
        LocalDate dateEcheance,

        @Schema(description = "Écart kilométrique constaté", example = "8500")
        Integer ecartKm,

        @Schema(description = "Écart en jours constaté", example = "30")
        Integer ecartJours,

        @Schema(description = "Détail lisible", example = "Échéance dépassée")
        String detail) {
}

package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Affectation consultée depuis une fiche salarié (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Affectation consultée depuis une fiche salarié (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : SalarieService.
 * NE FAIT PAS  : d'accès base.
 */
@Schema(description = "Affectation d'un salarié avec les dates et le véhicule")
public record AffectationSalarieResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’affectation
        @Schema(description = "Identifiant", example = "1")
        Integer idAffectation,
        // Identifiant du véhicule
        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,
        // Immatriculation
        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,
        // Date de début
        @Schema(description = "Date de début", example = "2026-11-01")
        LocalDate dateDebut,
        // Date de fin
        @Schema(description = "Date de fin", example = "2026-11-30")
        LocalDate dateFin,
        // En cours
        @Schema(description = "Affectation en cours", example = "true")
        Boolean enCours
) {

}

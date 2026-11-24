package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Affectation consultée depuis une fiche salarié (F7).
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * PRODUIT PAR : SalarieService.
 * NE FAIT PAS : d'accès base.
 */
@Schema(description = "Affectation d'un salarié avec les dates et le véhicule")
public record AffectationSalarieResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idAffectation,

        @Schema(description = "Véhicule", example = "1")
        Integer idVehicule,

        @Schema(description = "Immatriculation", example = "AB-123-CD")
        String immatriculation,

        @Schema(description = "Date de début", example = "2026-11-01")
        LocalDate dateDebut,

        @Schema(description = "Date de fin", example = "2026-11-30")
        LocalDate dateFin,

        @Schema(description = "Affectation en cours", example = "true")
        Boolean enCours) {
}

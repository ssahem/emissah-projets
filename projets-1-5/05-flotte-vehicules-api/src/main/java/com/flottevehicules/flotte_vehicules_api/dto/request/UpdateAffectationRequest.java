package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Corps de modification d'une affectation (F4) : la date de fin est modifiable ;
 * véhicule et salarié sont figés dès le début de l'affectation.
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * NE FAIT PAS : de logique métier.
 */
@Schema(description = "Modification de la date de fin et, avant le début, des parties de l'affectation")
public record UpdateAffectationRequest(

        @Schema(description = "Nouvelle date de fin", example = "2026-12-10")
        LocalDate dateFin,

        @Schema(description = "Véhicule, modifiable seulement avant le début", example = "2")
        Integer idVehicule,

        @Schema(description = "Salarié, modifiable seulement avant le début", example = "3")
        Integer idSalarie) {
}

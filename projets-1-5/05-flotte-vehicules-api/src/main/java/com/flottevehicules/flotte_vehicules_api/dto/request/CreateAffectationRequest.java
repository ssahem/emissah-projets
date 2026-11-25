package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Corps de création d'une affectation (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une affectation (F4).
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de contrôle de chevauchement (rôle du service).
 */
@Schema(description = "Création d'une affectation : véhicule, salarié et date de début requis")
public record CreateAffectationRequest(

        @Schema(description = "Véhicule concerné", example = "1")
        @NotNull(message = "Le véhicule est obligatoire")
        Integer idVehicule,

        @Schema(description = "Salarié concerné", example = "1")
        @NotNull(message = "Le salarié est obligatoire")
        Integer idSalarie,

        @Schema(description = "Date de début", example = "2026-11-01")
        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        @Schema(description = "Date de fin prévue, facultative", example = "2026-11-30")
        LocalDate dateFin) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

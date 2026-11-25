package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps de création d'un entretien (F5). D-02 : « la date » est `date`, mappée sur
 *   date_debut.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'un entretien (F5). D-02 : « la date » est `date`, mappée sur date_debut.
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de contrôle de kilométrage (rôle du service, D-01).
 */
@Schema(description = "Création d'un entretien : véhicule, date, type et coût positif requis")
public record CreateEntretienRequest(

        @Schema(description = "Date de début de l'immobilisation", example = "2026-11-10")
        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @Schema(description = "Date de fin de l'immobilisation, facultative", example = "2026-11-12")
        LocalDate dateFin,

        @Schema(description = "Véhicule concerné", example = "1")
        @NotNull(message = "Le véhicule est obligatoire")
        Integer idVehicule,

        @Schema(description = "Type d'entretien", example = "1")
        @NotNull(message = "Le type d'entretien est obligatoire")
        Integer idTypeEntretien,

        @Schema(description = "Prestataire", example = "1")
        @NotNull(message = "Le prestataire est obligatoire")
        Integer idPrestataire,

        @Schema(description = "Coût strictement positif", example = "320.00")
        @NotNull(message = "Le coût est obligatoire")
        @Positive(message = "Le coût doit être strictement positif")
        BigDecimal cout,

        @Schema(description = "Kilométrage relevé", example = "19000")
        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage ne peut pas être négatif")
        Integer km) {

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

}

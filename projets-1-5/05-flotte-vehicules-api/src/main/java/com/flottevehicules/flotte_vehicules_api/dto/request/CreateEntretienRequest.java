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

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Date
        @Schema(description = "Date de début de l'immobilisation", example = "2026-11-10")
        @NotNull(message = "La date est obligatoire")
        LocalDate date,
        // Date de fin
        @Schema(description = "Date de fin de l'immobilisation, facultative", example = "2026-11-12")
        LocalDate dateFin,
        // Identifiant du véhicule
        @Schema(description = "Véhicule concerné", example = "1")
        @NotNull(message = "Le véhicule est obligatoire")
        Integer idVehicule,
        // Identifiant du type d’entretien
        @Schema(description = "Type d'entretien", example = "1")
        @NotNull(message = "Le type d'entretien est obligatoire")
        Integer idTypeEntretien,
        // Identifiant du prestataire
        @Schema(description = "Prestataire", example = "1")
        @NotNull(message = "Le prestataire est obligatoire")
        Integer idPrestataire,
        // Coût
        @Schema(description = "Coût strictement positif", example = "320.00")
        @NotNull(message = "Le coût est obligatoire")
        @Positive(message = "Le coût doit être strictement positif")
        BigDecimal cout,
        // Kilométrage
        @Schema(description = "Kilométrage relevé", example = "19000")
        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage ne peut pas être négatif")
        Integer km
) {

}

package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * Corps d'une restitution de véhicule (F13).
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * NE FAIT PAS : de contrôle de date ni de kilométrage (rôle du service).
 */
@Schema(description = "Restitution : date postérieure au début et non future, kilométrage croissant (D-01)")
public record CreateRestitutionRequest(

        @Schema(description = "Date de restitution", example = "2026-11-24")
        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @Schema(description = "Kilométrage relevé à la restitution", example = "19000")
        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage ne peut pas être négatif")
        Integer km) {
}

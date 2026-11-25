package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps de modification d'un entretien (F5) : coût et prestataire modifiables,
 * date et kilométrage figés (une valeur différente est refusée en 409).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un entretien (F5) : coût et prestataire modifiables, date et kilométrage figés (une valeur différente est refusée en 409).
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification du coût et du prestataire ; date et kilométrage figés")
public record UpdateEntretienRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Coût
        @Schema(description = "Nouveau coût", example = "350.00")
        @NotNull(message = "Le coût est obligatoire")
        @Positive(message = "Le coût doit être strictement positif")
        BigDecimal cout,
        // Identifiant du prestataire
        @Schema(description = "Nouveau prestataire", example = "2")
        @NotNull(message = "Le prestataire est obligatoire")
        Integer idPrestataire,
        // Date
        @Schema(description = "Date inchangée, transmise pour détection d'une tentative de modification", example = "2026-11-10")
        LocalDate date,
        // Kilométrage
        @Schema(description = "Kilométrage inchangé, transmis pour détection d'une tentative de modification", example = "19000")
        Integer km
) {

}

package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Nouveau créneau d'une intervention de reprise (F15).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : RepriseController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Reprogrammation d'une intervention échouée")
public record CreateRepriseRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Schema(description = "Nouveau début prévu", example = "2026-12-10T09:00:00")
    @NotNull LocalDateTime debutPrevu,

    @Schema(description = "Nouvelle fin prévue", example = "2026-12-10T12:00:00")
    @NotNull LocalDateTime finPrevue) {
}

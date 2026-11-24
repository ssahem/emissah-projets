package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Corps de création d'une intervention (F5).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : InterventionController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Création d'une intervention")
public record CreateInterventionRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Schema(description = "Identifiant du client", example = "1")
    @NotNull @Positive Integer idClient,

    @Schema(description = "Code du type d'intervention", example = "RACCORDEMENT_FIBRE")
    @NotBlank @Size(max = 100) String codeType,

    @Schema(description = "Identifiant du technicien responsable", example = "1")
    @NotNull @Positive Integer idTechnicienResponsable,

    @Schema(description = "Date et heure de la demande", example = "2026-11-24T09:00:00")
    @NotNull LocalDateTime dateDemande,

    @Schema(description = "Début du créneau prévu", example = "2026-12-01T09:00:00")
    @NotNull LocalDateTime debutPrevu,

    @Schema(description = "Fin du créneau prévu", example = "2026-12-01T12:00:00")
    @NotNull LocalDateTime finPrevue,

    @Schema(description = "Voie du lieu d'intervention", example = "12 rue de la Paix")
    @NotBlank @Size(max = 255) String voie,

    @Schema(description = "Commune du lieu d'intervention", example = "Paris")
    @NotBlank @Size(max = 100) String commune) {
}

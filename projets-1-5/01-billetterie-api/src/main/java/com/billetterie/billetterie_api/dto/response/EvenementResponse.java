package com.billetterie.billetterie_api.dto.response;

import com.billetterie.billetterie_api.enums.TypeSpectacle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Événement renvoyé par l'API. COUCHE : DTO (réponse) · PRODUIT PAR : EvenementMapper. */
@Schema(description = "Événement et son spectacle")
public record EvenementResponse(
        @Schema(description = "Identifiant", example = "1") Integer idEvenement,
        @Schema(description = "Titre", example = "Concert Jazz") String titre,
        @Schema(description = "Type", example = "CONCERT") TypeSpectacle type,
        @Schema(description = "Places numérotées", example = "true") Boolean placementNumerote,
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00") LocalDateTime dateHeure,
        @Schema(description = "Identifiant de la salle", example = "1") Integer idSalle,
        @Schema(description = "Identifiant du spectacle", example = "1") Integer idSpectacle) {
}

package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** Événement d'une programmation (F7). COUCHE : DTO (réponse) · PRODUIT PAR : EvenementMapper. */
@Schema(description = "Événement d'une salle, avec l'indicateur « à venir »")
public record ProgrammationResponse(
        @Schema(description = "Identifiant", example = "1") Integer idEvenement,
        @Schema(description = "Titre", example = "Concert Jazz") String titre,
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00") LocalDateTime dateHeure,
        @Schema(description = "Vrai si la date est future", example = "true") boolean avenir) {
}

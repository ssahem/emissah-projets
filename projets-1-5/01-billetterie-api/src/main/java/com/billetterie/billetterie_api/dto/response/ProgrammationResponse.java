package com.billetterie.billetterie_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Événement d'une programmation (F7).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Événement d'une programmation (F7).
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : ProgrammationMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Événement d'une salle, avec l'indicateur « à venir »")
public record ProgrammationResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant
        @Schema(description = "Identifiant", example = "1") Integer idEvenement,
        // Titre
        @Schema(description = "Titre", example = "Concert Jazz") String titre,
        // Date ISO-8601
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00") LocalDateTime dateHeure,
        // Vrai si la date est future
        @Schema(description = "Vrai si la date est future", example = "true") boolean avenir) {
}

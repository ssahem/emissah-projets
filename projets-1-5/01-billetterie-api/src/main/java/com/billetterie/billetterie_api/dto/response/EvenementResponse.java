package com.billetterie.billetterie_api.dto.response;

import com.billetterie.billetterie_api.enums.TypeSpectacle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Événement renvoyé par l'API.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Événement renvoyé par l'API.
 * IMMUABLE     : record Java.
 * PRODUIT PAR  : EvenementMapper.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Événement et son spectacle")
public record EvenementResponse(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de l’evenement
        @Schema(description = "Identifiant", example = "1")
        Integer idEvenement,
        // Titre
        @Schema(description = "Titre", example = "Concert Jazz")
        String titre,
        // Type
        @Schema(description = "Type", example = "CONCERT")
        TypeSpectacle type,
        // Placement numéroté
        @Schema(description = "Places numérotées", example = "true")
        Boolean placementNumerote,
        // Date heure
        @Schema(description = "Date ISO-8601", example = "2026-12-20T20:00:00")
        LocalDateTime dateHeure,
        // Identifiant de la salle
        @Schema(description = "Identifiant de la salle", example = "1")
        Integer idSalle,
        // Identifiant du spectacle
        @Schema(description = "Identifiant du spectacle", example = "1")
        Integer idSpectacle
) {
}

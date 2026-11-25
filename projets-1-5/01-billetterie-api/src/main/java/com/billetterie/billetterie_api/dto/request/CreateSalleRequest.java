package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'une salle.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une salle.
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Création d'une salle")
public record CreateSalleRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Nom de la salle
        @Schema(description = "Nom de la salle", example = "Grand Hall") @NotBlank @Size(max = 100) String nom,
        // Adresse postale
        @Schema(description = "Adresse postale", example = "12 rue des Arts") @NotBlank @Size(max = 255) String adresse,
        // Ville
        @Schema(description = "Ville", example = "Abidjan") @NotBlank @Size(max = 100) String ville,
        // Capacité
        @Schema(description = "Capacité", example = "120") @NotNull @Positive Integer capacite) {
}

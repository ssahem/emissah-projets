package com.billetterie.billetterie_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corps de création d'une place.
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de création d'une place.
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : le controller, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d’accès base ni de logique métier.
 */
@Schema(description = "Création d'une place")
public record CreatePlaceRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Identifiant de la salle
        @Schema(description = "Identifiant de la salle", example = "1") @NotNull @Positive Integer idSalle,
        // Rang
        @Schema(description = "Rang", example = "A") @NotBlank @Size(max = 5) String rang,
        // Numéro
        @Schema(description = "Numéro", example = "13") @NotNull @Positive Integer numero,
        // Catégorie
        @Schema(description = "Catégorie", example = "ORCHESTRE") @NotBlank @Size(max = 20) String categorie) {
}

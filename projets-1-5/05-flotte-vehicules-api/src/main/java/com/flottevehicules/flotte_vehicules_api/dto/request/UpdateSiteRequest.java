package com.flottevehicules.flotte_vehicules_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de modification d'un site : seuls l'adresse et la ville sont modifiables (F1).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un site : seuls l'adresse et la ville sont modifiables (F1).
 * IMMUABLE     : record Java.
 * NE FAIT PAS  : de logique métier.
 */
@Schema(description = "Modification de l'adresse et de la ville d'un site")
public record UpdateSiteRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Adresse
        @Schema(description = "Nouvelle adresse", example = "14 rue de la Paix")
        @NotBlank(message = "L'adresse est obligatoire")
        String adresse,
        // Ville
        @Schema(description = "Nouvelle ville", example = "Paris")
        @NotBlank(message = "La ville est obligatoire")
        String ville
) {

}

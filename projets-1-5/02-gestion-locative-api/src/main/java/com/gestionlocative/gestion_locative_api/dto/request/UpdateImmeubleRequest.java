package com.gestionlocative.gestion_locative_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'un immeuble (F2).
 *
 * COUCHE       : DTO (contrat public)
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ImmeubleController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un immeuble")
public record UpdateImmeubleRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

        // Adresse
        @Schema(description = "Adresse", example = "12 rue des Acacias")
        @NotBlank @Size(max = 255) String adresse,
        // Code postal
        @Schema(description = "Code postal", example = "75001")
        @NotBlank @Size(max = 10) String codePostal,
        // Ville
        @Schema(description = "Ville", example = "Paris")
        @NotBlank @Size(max = 100) String ville,
        // Propriétaire modifiable en cas de changement de mandat
        @Schema(description = "Identifiant du propriétaire", example = "1")
        @NotNull @Positive Integer idProprietaire,
        // Nombre d'étages
        @Schema(description = "Nombre d'étages", example = "3")
        @NotNull @Positive Integer nbEtages) {
}

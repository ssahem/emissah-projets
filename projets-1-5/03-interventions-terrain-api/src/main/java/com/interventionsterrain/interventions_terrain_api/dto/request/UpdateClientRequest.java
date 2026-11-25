package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de modification d'un client (F4).
 *
 * COUCHE       : DTO (contrat public)
 * RÔLE         : Corps de modification d'un client (F4).
 * IMMUABLE     : record Java.
 * UTILISÉ PAR  : ClientController, avec @Valid, pour valider le corps reçu.
 * NE FAIT PAS  : d'accès base ni de logique métier.
 */
@Schema(description = "Modification d'un client")
public record UpdateClientRequest(

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    @Schema(description = "Nom", example = "Dupont")
    @NotBlank @Size(max = 100) String nom,

    @Schema(description = "Prénom", example = "Marie")
    @Size(max = 100) String prenom,

    @Schema(description = "Email", example = "marie.dupont@example.test")
    @NotBlank @Email @Size(max = 255) String email,

    @Schema(description = "Téléphone", example = "0102030407")
    @NotBlank @Size(max = 20) String telephone,

    @Schema(description = "Adresse du client", example = "5 avenue du parc")
    @NotBlank @Size(max = 255) String adresse,

    @Schema(description = "Commune du client", example = "Paris")
    @NotBlank @Size(max = 100) String commune) {
}

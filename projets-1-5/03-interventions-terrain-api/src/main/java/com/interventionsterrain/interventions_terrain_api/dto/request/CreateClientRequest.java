package com.interventionsterrain.interventions_terrain_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de création d'un client (F4). */
@Schema(description = "Création d'un client")
public record CreateClientRequest(

    @Schema(description = "Nom", example = "Dupont")
    @NotBlank @Size(max = 100) String nom,

    @Schema(description = "Prénom (facultatif)", example = "Marie")
    @Size(max = 100) String prenom,

    @Schema(description = "Email (facultatif : valeur unique générée si absent)")
    @Email @Size(max = 255) String email,

    @Schema(description = "Téléphone", example = "0102030407")
    @NotBlank @Size(max = 20) String telephone,

    @Schema(description = "Adresse du client (texte, sans FK)", example = "5 avenue du parc")
    @NotBlank @Size(max = 255) String adresse,

    @Schema(description = "Commune du client (texte, sans FK)", example = "Paris")
    @NotBlank @Size(max = 100) String commune) {
}

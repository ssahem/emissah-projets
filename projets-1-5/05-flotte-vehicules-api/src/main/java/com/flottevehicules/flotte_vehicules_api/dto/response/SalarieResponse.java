package com.flottevehicules.flotte_vehicules_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de sortie d'une fiche salarié (F3).
 *
 * COUCHE      : DTO (contrat public)
 * IMMUABLE    : record Java.
 * PRODUIT PAR : SalarieMapper.
 * NE FAIT PAS : d'accès base ni de logique métier.
 */
@Schema(description = "Salarié et son service")
public record SalarieResponse(

        @Schema(description = "Identifiant", example = "1")
        Integer idSalarie,

        @Schema(description = "Nom", example = "Martin")
        String nom,

        @Schema(description = "Prénom", example = "Alice")
        String prenom,

        @Schema(description = "Adresse électronique", example = "alice.martin@example.test")
        String email,

        @Schema(description = "Matricule unique", example = "MAT001")
        String matricule,

        @Schema(description = "Service", example = "Direction")
        String service) {
}
